# Purpose: Centrally and securely manage secrets like database credentials, API keys, and certificates using AWS Secrets Manager.
# LogicDescription: Creates AWS Secrets Manager secret resources. Supports creating secrets with string values
# or generating random passwords. Configures secret rotation policies where applicable (e.g., for RDS credentials).
# Manages resource policies for secrets to control access.
# Requirement REQ-8-011

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
}

resource "aws_secretsmanager_secret" "main" {
  for_each = var.secrets

  name        = each.value.name
  description = lookup(each.value, "description", "Managed by Terraform")
  kms_key_id  = lookup(each.value, "kms_key_id", null)
  tags        = merge(var.common_tags, lookup(each.value, "tags", {}))

  # Forcing delete without recovery for non-prod for easier cleanup if needed
  force_overwrite_replica_secret = lookup(each.value, "force_overwrite_replica_secret", false)
  recovery_window_in_days      = lookup(each.value, "recovery_window_in_days", var.default_recovery_window_in_days)
}

resource "aws_secretsmanager_random_password" "generated" {
  for_each = { for k, v in var.secrets : k => v if lookup(v, "generate_random_password", false) }

  password_length           = lookup(each.value.random_password_config, "length", 32)
  exclude_characters        = lookup(each.value.random_password_config, "exclude_characters", "/@\"\\")
  exclude_numbers           = lookup(each.value.random_password_config, "exclude_numbers", false)
  exclude_punctuation       = lookup(each.value.random_password_config, "exclude_punctuation", false)
  exclude_uppercase         = lookup(each.value.random_password_config, "exclude_uppercase", false)
  exclude_lowercase         = lookup(each.value.random_password_config, "exclude_lowercase", false)
  include_space             = lookup(each.value.random_password_config, "include_space", false)
  require_each_included_type = lookup(each.value.random_password_config, "require_each_included_type", true)
}

resource "aws_secretsmanager_secret_version" "main" {
  for_each = var.secrets

  secret_id     = aws_secretsmanager_secret.main[each.key].id
  secret_string = lookup(each.value, "generate_random_password", false) ? aws_secretsmanager_random_password.generated[each.key].random_password : lookup(each.value, "secret_string", null)
  secret_binary = lookup(each.value, "secret_binary", null) # Base64 encoded binary string

  # Ensure this only runs if secret_string or secret_binary is provided and not using random password for this version
  lifecycle {
    ignore_changes = [
      # If random password is used, the value is set by aws_secretsmanager_random_password resource
      # and this resource might try to update it if not ignored.
      # However, the for_each condition on aws_secretsmanager_random_password should handle this.
      # This specific aws_secretsmanager_secret_version is for setting an initial static value,
      # or for a random password. The ternary operator above handles which value to use.
    ]
  }
}

resource "aws_secretsmanager_secret_policy" "main" {
  for_each = { for k, v in var.secrets : k => v if lookup(v, "policy", null) != null }

  secret_arn = aws_secretsmanager_secret.main[each.key].arn
  policy     = each.value.policy # IAM policy JSON string

  block_public_policy = lookup(each.value, "block_public_policy", true) # Best practice
}

resource "aws_secretsmanager_secret_rotation" "main" {
  for_each = { for k, v in var.secrets : k => v if lookup(v, "rotation_lambda_arn", null) != null }

  secret_id           = aws_secretsmanager_secret.main[each.key].id
  rotation_lambda_arn = each.value.rotation_lambda_arn

  rotation_rules {
    automatically_after_days = lookup(each.value.rotation_rules, "automatically_after_days", 30)
    duration                 = lookup(each.value.rotation_rules, "duration", null) # e.g., "2h"
    schedule_expression      = lookup(each.value.rotation_rules, "schedule_expression", null) # e.g., "cron(0 4 ? * MON *)"
  }

  depends_on = [aws_secretsmanager_secret_version.main]
}