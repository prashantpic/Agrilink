# Purpose: Define a reusable IAM policy document granting permissions for services/roles to read specific secrets from AWS Secrets Manager.
# LogicDescription: Uses a Terraform data source 'aws_iam_policy_document' to render an IAM policy JSON.
# The policy grants 'secretsmanager:GetSecretValue', 'secretsmanager:DescribeSecret', 'secretsmanager:ListSecrets'
# permissions on a list of specified secret ARNs (passed as a variable).
# Requirement: REQ-8-011

variable "secret_arns" {
  description = "A list of ARNs of AWS Secrets Manager secrets to grant access to."
  type        = list(string)
  default     = []
}

variable "allow_list_secrets" {
  description = "Whether to allow ListSecrets action on all secrets (use with caution, or provide specific resource for ListSecrets)."
  type        = bool
  default     = false # Typically ListSecrets is broader, GetSecretValue/DescribeSecret are per-secret
}

variable "list_secrets_resource_arn" {
  description = "Specific resource ARN for ListSecrets action, e.g., \"arn:aws:secretsmanager:REGION:ACCOUNT_ID:secret:some_prefix*\". Defaults to \"*\" if allow_list_secrets is true and this is not set."
  type        = string
  default     = "*"
}


data "aws_iam_policy_document" "secrets_manager_access" {
  statement {
    sid    = "AllowReadSpecificSecrets"
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret"
    ]
    resources = var.secret_arns
  }

  dynamic "statement" {
    for_each = var.allow_list_secrets ? [1] : []
    content {
      sid    = "AllowListSecrets"
      effect = "Allow"
      actions = [
        "secretsmanager:ListSecrets"
      ]
      # Restrict ListSecrets to specific prefix if possible, or "*" if broad access is intended and accepted.
      # Using "*" for ListSecrets can be a security risk if not intended.
      resources = [var.list_secrets_resource_arn]
    }
  }
}

# Output the policy document JSON
output "policy_json" {
  description = "The IAM policy document JSON for accessing specified Secrets Manager secrets."
  value       = data.aws_iam_policy_document.secrets_manager_access.json
}