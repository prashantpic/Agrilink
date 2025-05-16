# Purpose: Define a reusable IAM policy document granting permissions for services/roles
# to read specific secrets from AWS Secrets Manager.
# LogicDescription: Uses a Terraform data source 'aws_iam_policy_document' to render
# an IAM policy JSON. The policy grants 'secretsmanager:GetSecretValue' permissions
# on a list of specified secret ARNs (passed as a variable).
# ImplementedFeatures: Secrets Read Access Policy
# RequirementIds: REQ-8-011

# This file defines a data source for an IAM policy document.
# It expects a variable `secret_arns` to be provided in the scope where this data source is used.
# Example usage in a module:
#
# module "secrets_policy_doc" {
#   source = "../shared/iam/policies/secrets_manager_access_policy.json.tf" # Incorrect way to "call" a data source like this
#   secret_arns = ["arn:aws:secretsmanager:us-east-1:123456789012:secret:mysecret-xxxxxx"]
# }
#
# resource "aws_iam_policy" "example_policy" {
#   name   = "example-secrets-access-policy"
#   policy = data.aws_iam_policy_document.secrets_manager_access.json # Use directly, variables passed to data source
# }

# To make this truly reusable like a module, it would need to be structured as a module.
# As a ".json.tf" file, it's typically used to generate a JSON string within another .tf file.
# Let's assume this is intended to be directly used as a data source definition.

# This data source itself doesn't take input variables in the typical module sense.
# Instead, its configuration directly uses variables that must be in scope or passed.
# For this shared policy, it's better to make it a data source that takes `secret_arns`
# as part of its configuration block, or use `template_file` if it's more complex.
# The `CodeFileDefinition` mentions "passed as a variable to the module/resource that uses this policy document".
# This means the `secret_arns` variable is expected to be defined by the consumer.

# This file provides the `data "aws_iam_policy_document"` block.
# Variables needed: `var.secret_arns_for_policy` (list of strings)

data "aws_iam_policy_document" "secrets_manager_access" {
  # This data block would be included in a .tf file that also defines `var.secret_arns_for_policy`
  # or where `var.secret_arns_for_policy` is passed.

  statement {
    sid = "AllowReadSpecificSecrets"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret" # Often needed to check existence or metadata
    ]
    resources = var.secret_arns_for_policy # This variable must be defined in the calling context
    effect    = "Allow"
  }

  # Optionally, allow listing secrets if needed by the service (e.g., ExternalSecrets listing by path)
  # statement {
  #   sid = "AllowListSecrets"
  #   actions = [
  #     "secretsmanager:ListSecrets"
  #   ]
  #   resources = ["*"] # ListSecrets usually requires "*" or specific region resource
  #   effect    = "Allow"
  #   # condition {  # Optional: restrict by path if ListSecrets supports it well enough
  #   #   test     = "StringLike"
  #   #   variable = "secretsmanager:Name"
  #   #   values   = ["${var.project_name}/${var.environment}/*"]
  #   # }
  # }
}

# To use this in another .tf file:
#
# variable "secret_arns_for_policy" {
#   description = "List of secret ARNs to grant access to."
#   type        = list(string)
# }
#
# data "aws_iam_policy_document" "secrets_manager_access" {
#   # ... content from above ...
# }
#
# resource "aws_iam_policy" "my_app_secrets_policy" {
#   name   = "my-app-secrets-access"
#   policy = data.aws_iam_policy_document.secrets_manager_access.json
# }
#
# This file itself is not directly "applied". It's a definition.
# The output should be the content of this data source block.