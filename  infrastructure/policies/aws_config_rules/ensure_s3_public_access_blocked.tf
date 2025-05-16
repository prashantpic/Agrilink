# Purpose: Enforce security best practices for S3 buckets by ensuring public access is blocked.
# LogicDescription: Defines AWS Config rules using managed AWS Config rules
# (e.g., 's3-bucket-public-read-prohibited', 's3-bucket-public-write-prohibited').
# Configures the rule scope and parameters. Optionally, sets up remediation actions.
# Requirement: REQ-8-011 (Note: This req is for secrets, applying to S3 public access is likely a spec detail)

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
}

variable "s3_public_read_prohibited_rule_name" {
  description = "Name for the S3 public read prohibited AWS Config rule."
  type        = string
  default     = "s3-bucket-public-read-prohibited"
}

variable "s3_public_write_prohibited_rule_name" {
  description = "Name for the S3 public write prohibited AWS Config rule."
  type        = string
  default     = "s3-bucket-public-write-prohibited"
}

variable "s3_block_public_access_enabled_rule_name" {
  description = "Name for the S3 block public access enabled AWS Config rule."
  type        = string
  default     = "s3-account-level-public-access-blocks"
}


variable "enable_s3_public_read_prohibited_rule" {
  description = "Enable the S3 public read prohibited rule."
  type        = bool
  default     = true
}

variable "enable_s3_public_write_prohibited_rule" {
  description = "Enable the S3 public write prohibited rule."
  type        = bool
  default     = true
}

variable "enable_s3_block_public_access_enabled_rule" {
  description = "Enable the S3 block public access at account level rule."
  type        = bool
  default     = true
}

variable "config_rule_tags" {
  description = "Tags to apply to the AWS Config rules."
  type        = map(string)
  default     = {}
}

resource "aws_config_config_rule" "s3_bucket_public_read_prohibited" {
  count = var.enable_s3_public_read_prohibited_rule ? 1 : 0
  name  = var.s3_public_read_prohibited_rule_name
  tags  = var.config_rule_tags

  source {
    owner             = "AWS"
    source_identifier = "S3_BUCKET_PUBLIC_READ_PROHIBITED"
  }

  # Optional: Scope to specific resources or tags
  # scope {
  #   compliance_resource_types = ["AWS::S3::Bucket"]
  # }

  # Optional: Input parameters if the managed rule supports them
  # input_parameters = jsonencode({})

  # Optional: Maximum execution frequency
  # maximum_execution_frequency = "TwentyFour_Hours"
}

resource "aws_config_config_rule" "s3_bucket_public_write_prohibited" {
  count = var.enable_s3_public_write_prohibited_rule ? 1 : 0
  name  = var.s3_public_write_prohibited_rule_name
  tags  = var.config_rule_tags

  source {
    owner             = "AWS"
    source_identifier = "S3_BUCKET_PUBLIC_WRITE_PROHIBITED"
  }
}

resource "aws_config_config_rule" "s3_account_level_public_access_blocks" {
  count = var.enable_s3_block_public_access_enabled_rule ? 1 : 0
  name  = var.s3_block_public_access_enabled_rule_name
  tags  = var.config_rule_tags

  source {
    owner             = "AWS"
    source_identifier = "S3_ACCOUNT_LEVEL_PUBLIC_ACCESS_BLOCKS"
  }
  # This rule typically does not have input parameters that need setting at the rule level,
  # as it checks the account-level S3 Public Access Block settings.
}

# Note on Remediation:
# Remediation configuration (`aws_config_remediation_configuration`) can be added
# to automatically fix non-compliant resources. This requires an SSM Automation document
# and an IAM role for Config to execute the remediation.
# Example:
# resource "aws_config_remediation_configuration" "s3_read_remediation" {
#   count = var.enable_s3_public_read_prohibited_rule && var.enable_remediation ? 1 : 0
#
#   config_rule_name = aws_config_config_rule.s3_bucket_public_read_prohibited[0].name
#   target_id        = "AWS-DisableS3BucketPublicReadWrite" # Example AWS-managed SSM document
#   target_type      = "SSM_DOCUMENT"
#   automatic        = true
#
#   parameter {
#     name           = "AutomationAssumeRole"
#     resource_value = aws_iam_role.config_remediation_role.arn # Role for Config to assume
#   }
#   parameter {
#     name          = "S3BucketName"
#     resource_value = "parameterName" # Special value to pass the resource ID
#   }
# }
# This would require defining `aws_iam_role.config_remediation_role` and potentially custom SSM docs.