# Purpose: Automate and centralize backup processes for various AWS resources.
# LogicDescription: Creates AWS Backup vaults, backup plans (frequency, retention, lifecycle),
# and assigns resources to plans using tags or ARNs.

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
}

resource "aws_backup_vault" "main" {
  name        = var.vault_name
  kms_key_arn = var.kms_key_arn # Optional: for encryption with customer-managed KMS key

  tags = merge(
    var.common_tags,
    {
      Name = var.vault_name
    }
  )
}

resource "aws_iam_role" "backup_service_role" {
  count = var.create_iam_role ? 1 : 0
  name  = "${var.vault_name}-backup-service-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "backup.amazonaws.com"
        }
      }
    ]
  })
  tags = var.common_tags
}

resource "aws_iam_role_policy_attachment" "backup_service_role_policy" {
  count      = var.create_iam_role ? 1 : 0
  role       = aws_iam_role.backup_service_role[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForBackup"
  # If restores are also managed via AWS Backup console/API by this role:
  # policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForRestores"
}


resource "aws_backup_plan" "main" {
  name = var.plan_name
  tags = merge(
    var.common_tags,
    {
      Name = var.plan_name
    }
  )

  dynamic "rule" {
    for_each = var.backup_rules
    content {
      rule_name                = rule.value.name
      target_vault_name        = var.vault_name # Or rule.value.target_vault_name if configurable per rule
      schedule                 = rule.value.schedule # e.g., "cron(0 12 * * ? *)" for daily at 12 PM UTC
      start_window             = lookup(rule.value, "start_window", 480) # Minutes
      completion_window        = lookup(rule.value, "completion_window", 10080) # Minutes (7 days)
      enable_continuous_backup = lookup(rule.value, "enable_continuous_backup", false) # For PITR

      lifecycle {
        cold_storage_after = lookup(rule.value.lifecycle, "cold_storage_after", 0) # Days, 0 to disable
        delete_after       = lookup(rule.value.lifecycle, "delete_after", 90)     # Days
      }

      dynamic "copy_action" {
        for_each = lookup(rule.value, "copy_actions", [])
        content {
          destination_vault_arn = copy_action.value.destination_vault_arn
          lifecycle {
            cold_storage_after = lookup(copy_action.value.lifecycle, "cold_storage_after", 0)
            delete_after       = lookup(copy_action.value.lifecycle, "delete_after", 0)
          }
        }
      }
      # recovery_point_tags = {} # Optional
    }
  }

  dynamic "advanced_backup_setting" {
    for_each = var.advanced_backup_settings
    content {
      backup_options = advanced_backup_setting.value.backup_options
      resource_type  = advanced_backup_setting.value.resource_type # e.g. "EC2" or "RDS"
    }
  }
  depends_on = [aws_backup_vault.main, aws_iam_role.backup_service_role]
}

resource "aws_backup_selection" "main" {
  for_each = var.backup_selections

  name            = each.value.name
  plan_id         = aws_backup_plan.main.id
  iam_role_arn    = var.create_iam_role ? aws_iam_role.backup_service_role[0].arn : var.backup_iam_role_arn # Use created role or provided one

  dynamic "condition" {
    for_each = lookup(each.value, "conditions", [])
    content {
      dynamic "string_equals" {
        for_each = lookup(condition.value, "string_equals", [])
        content {
          key   = string_equals.value.key
          value = string_equals.value.value
        }
      }
      dynamic "string_like" {
        for_each = lookup(condition.value, "string_like", [])
        content {
          key   = string_like.value.key
          value = string_like.value.value
        }
      }
      # Add other condition types (string_not_equals, string_not_like) if needed
    }
  }

  # Use either list of ARNs or tag-based selection, not both in the same selection normally.
  # The API supports selection_tag or resources. Terraform provider might require one or the other.
  # If both `resources` and `selection_tag` are provided, `resources` takes precedence.
  resources = lookup(each.value, "resource_arns", null)

  dynamic "selection_tag" { # For tag-based resource selection
    for_each = lookup(each.value, "resource_tags", null) != null ? [each.value.resource_tags] : [] # Wrap in list if not null
    content {
      type  = selection_tag.value.type # "STRINGEQUALS"
      key   = selection_tag.value.key
      value = selection_tag.value.value
    }
  }
  # not_resources = lookup(each.value, "not_resource_arns", null) # If supported by provider

  tags = merge(
    var.common_tags,
    lookup(each.value, "tags", {}),
    {
      Name = each.value.name
    }
  )
  depends_on = [aws_backup_plan.main]
}