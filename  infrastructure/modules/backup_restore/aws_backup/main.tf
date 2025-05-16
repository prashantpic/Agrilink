# Purpose: Automate and centralize backup processes for various AWS resources.
# LogicDescription: Creates AWS Backup vaults, backup plans (frequency, retention,
# lifecycle rules), and assigns resources to backup plans.
# ImplementedFeatures: AWS Backup Vault Creation, Backup Plan Definition,
# Resource Assignment to Backup Plans, Backup Retention and Lifecycle Policies.

locals {
  tags = var.tags
}

resource "aws_backup_vault" "main" {
  name        = "${var.project_name}-${var.environment}-backup-vault"
  kms_key_arn = var.kms_key_arn_for_vault # Optional: if null, uses AWS managed key for Backup

  tags = merge(local.tags, {
    Name = "${var.project_name}-${var.environment}-backup-vault"
  })
}

resource "aws_backup_plan" "main" {
  for_each = var.backup_plans

  name = "${var.project_name}-${var.environment}-${each.key}-plan"

  dynamic "rule" {
    for_each = each.value.rules
    content {
      rule_name                = rule.value.name
      target_vault_name        = aws_backup_vault.main.name # Use the vault created in this module
      schedule                 = rule.value.schedule      # e.g., "cron(0 5 * * ? *)"
      start_window             = rule.value.start_window_minutes
      completion_window        = rule.value.completion_window_minutes
      recovery_point_tags      = rule.value.recovery_point_tags
      enable_continuous_backup = rule.value.enable_continuous_backup # For PITR, if supported

      dynamic "lifecycle" {
        for_each = rule.value.lifecycle != null ? [rule.value.lifecycle] : []
        content {
          cold_storage_after = lifecycle.value.cold_storage_after_days
          delete_after     = lifecycle.value.delete_after_days
        }
      }

      dynamic "copy_action" {
        for_each = rule.value.copy_actions != null ? rule.value.copy_actions : []
        content {
          destination_vault_arn = copy_action.value.destination_vault_arn # ARN of another vault for cross-region/account copy
          dynamic "lifecycle" {
            for_each = copy_action.value.lifecycle != null ? [copy_action.value.lifecycle] : []
            content {
              cold_storage_after = lifecycle.value.cold_storage_after_days
              delete_after     = lifecycle.value.delete_after_days
            }
          }
        }
      }
    }
  }

  dynamic "advanced_backup_setting" {
     for_each = each.value.advanced_backup_settings != null ? each.value.advanced_backup_settings : []
     content {
        backup_options = advanced_backup_setting.value.backup_options # e.g. {"WindowsVSS" = "enabled"}
        resource_type  = advanced_backup_setting.value.resource_type  # e.g. "EC2"
     }
  }

  tags = merge(local.tags, {
    Name    = "${var.project_name}-${var.environment}-${each.key}-plan"
    PlanKey = each.key
  })
}

resource "aws_iam_role" "backup_service_role" {
  count = var.create_backup_service_role ? 1 : 0
  name  = "${var.project_name}-${var.environment}-backup-service-role"
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
  tags = merge(local.tags, {
    Name = "${var.project_name}-${var.environment}-backup-service-role"
  })
}

resource "aws_iam_role_policy_attachment" "backup_service_role_policy" {
  count      = var.create_backup_service_role ? 1 : 0
  role       = aws_iam_role.backup_service_role[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForBackup" # For backup operations
}

resource "aws_iam_role_policy_attachment" "backup_restore_service_role_policy" {
  count      = var.create_backup_service_role ? 1 : 0
  role       = aws_iam_role.backup_service_role[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForRestores" # For restore operations
}


resource "aws_backup_selection" "main" {
  for_each = var.backup_selections

  name            = "${var.project_name}-${var.environment}-${each.key}-selection"
  iam_role_arn    = var.create_backup_service_role ? aws_iam_role.backup_service_role[0].arn : var.backup_service_role_arn
  plan_id         = aws_backup_plan.main[each.value.plan_key_ref].id # Reference plan by its key

  # Selection can be by tags or by resource ARNs
  dynamic "selection_tag" {
    for_each = each.value.selection_method == "TAG" && each.value.tags != null ? each.value.tags : []
    content {
      type  = selection_tag.value.type # "STRINGEQUALS"
      key   = selection_tag.value.key
      value = selection_tag.value.value
    }
  }

  resources = each.value.selection_method == "RESOURCES" && each.value.resource_arns != null ? each.value.resource_arns : null

  # Optionally, specify conditions for tag-based selections
  # dynamic "condition" {
  #   for_each = each.value.conditions != null ? each.value.conditions : []
  #   content {
  #     # ...
  #   }
  # }

  tags = merge(local.tags, {
    Name         = "${var.project_name}-${var.environment}-${each.key}-selection"
    SelectionKey = each.key
  })
}