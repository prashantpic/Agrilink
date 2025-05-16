# Purpose: Centrally and securely manage secrets like database credentials,
# API keys, and certificates using AWS Secrets Manager.
# LogicDescription: Creates AWS Secrets Manager secret resources. Supports creating
# secrets with string values or generating random passwords. Configures secret
# rotation policies where applicable. Manages resource policies for secrets.
# ImplementedFeatures: AWS Secret Creation, Secret Value Management,
# Secret Rotation Configuration (Optional), Secret Policy Management
# RequirementIds: REQ-8-011

resource "aws_secretsmanager_secret" "main" {
  for_each = var.secrets

  name                    = "${var.project_name}-${var.environment}-${each.key}"
  description             = each.value.description
  recovery_window_in_days = each.value.recovery_window_in_days
  kms_key_id              = each.value.kms_key_id # Optional: Uses default AWS managed CMK if not specified

  tags = merge(
    var.tags,
    each.value.tags,
    {
      Name = "${var.project_name}-${var.environment}-${each.key}"
    }
  )
}

resource "aws_secretsmanager_secret_version" "initial_value" {
  for_each = { for k, v in var.secrets : k => v if v.initial_secret_string != null }

  secret_id     = aws_secretsmanager_secret.main[each.key].id
  secret_string = each.value.initial_secret_string
}

resource "aws_secretsmanager_secret_version" "random_password" {
  for_each = { for k, v in var.secrets : k => v if v.generate_random_password }

  secret_id     = aws_secretsmanager_secret.main[each.key].id
  secret_string = random_password.generated[each.key].result
}

resource "random_password" "generated" {
  for_each = { for k, v in var.secrets : k => v if v.generate_random_password }

  length           = each.value.random_password_config.length
  special          = each.value.random_password_config.special
  override_special = each.value.random_password_config.override_special
  min_lower        = each.value.random_password_config.min_lower
  min_upper        = each.value.random_password_config.min_upper
  min_numeric      = each.value.random_password_config.min_numeric
  min_special      = each.value.random_password_config.min_special
}

resource "aws_secretsmanager_secret_policy" "main" {
  for_each = { for k, v in var.secrets : k => v if v.policy_json != null }

  secret_arn = aws_secretsmanager_secret.main[each.key].arn
  policy     = each.value.policy_json

  block_public_policy = true # Recommended default
}

# Example for RDS rotation - this requires a Lambda function to be created/managed separately
# resource "aws_secretsmanager_secret_rotation" "rds_rotation" {
#   for_each = { for k, v in var.secrets : k => v if v.rotation_lambda_arn != null && v.rotation_rules != null }
#
#   secret_id           = aws_secretsmanager_secret.main[each.key].id
#   rotation_lambda_arn = each.value.rotation_lambda_arn
#
#   rotation_rules {
#     automatically_after_days = each.value.rotation_rules.automatically_after_days
#     schedule_expression      = each.value.rotation_rules.schedule_expression # e.g. "cron(0 10 * * ? *)"
#     duration                 = each.value.rotation_rules.duration           # e.g. "2h"
#   }
#
#   depends_on = [
#     aws_secretsmanager_secret_version.initial_value,
#     aws_secretsmanager_secret_version.random_password,
#   ]
# }