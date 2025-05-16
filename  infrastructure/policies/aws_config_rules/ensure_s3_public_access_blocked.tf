# Purpose: Enforce security best practices for S3 buckets by ensuring public access is blocked.
# LogicDescription: Defines an AWS Config rule using managed AWS Config rules
# (e.g., 's3-bucket-public-read-prohibited', 's3-bucket-public-write-prohibited').
# Configures the rule scope and parameters. Optionally, sets up remediation actions.
# ImplementedFeatures: S3 Public Access Block Policy Enforcement
# RequirementIds: REQ-8-011 (Indirectly contributes by enforcing secure S3 configurations)

# This configuration assumes AWS Config service is already enabled in the account/region.
# Variables expected from the calling environment/root configuration:
# - var.project_name (string)
# - var.environment (string)
# - var.tags (map of string)

resource "aws_config_config_rule" "s3_bucket_public_read_prohibited" {
  name = "${var.project_name}-${var.environment}-s3-public-read-prohibited"

  source {
    owner             = "AWS"
    source_identifier = "S3_BUCKET_PUBLIC_READ_PROHIBITED"
  }

  description      = "Checks that S3 buckets do not allow public read access. This rule is NON_COMPLIANT if an S3 bucket policy or bucket ACL allows public read access."
  input_parameters = jsonencode({}) # No specific input parameters usually required for this managed rule

  # Scope can be defined to target specific resources or all resources of a type
  # scope {
  #   compliance_resource_types = ["AWS::S3::Bucket"]
  #   # tag_key                     = "Environment"
  #   # tag_value                   = var.environment
  # }

  # Maximum execution frequency (e.g., One_Hour, Three_Hours, Six_Hours, Twelve_Hours, TwentyFour_Hours)
  maximum_execution_frequency = "TwentyFour_Hours"

  tags = merge(
    var.tags,
    {
      Name        = "${var.project_name}-${var.environment}-s3-public-read-prohibited"
      RuleType    = "Managed"
      Compliance  = "Security"
    }
  )
}

resource "aws_config_config_rule" "s3_bucket_public_write_prohibited" {
  name = "${var.project_name}-${var.environment}-s3-public-write-prohibited"

  source {
    owner             = "AWS"
    source_identifier = "S3_BUCKET_PUBLIC_WRITE_PROHIBITED"
  }

  description      = "Checks that S3 buckets do not allow public write access. This rule is NON_COMPLIANT if an S3 bucket policy or bucket ACL allows public write access."
  input_parameters = jsonencode({})

  maximum_execution_frequency = "TwentyFour_Hours"

  tags = merge(
    var.tags,
    {
      Name        = "${var.project_name}-${var.environment}-s3-public-write-prohibited"
      RuleType    = "Managed"
      Compliance  = "Security"
    }
  )
}

# Optional: S3 Block Public Access settings at the account level (via aws_s3_account_public_access_block)
# This is a more direct enforcement mechanism than just AWS Config detection.
# AWS Config rules are for detection and reporting (and optional remediation).
#
# resource "aws_s3_account_public_access_block" "account_block" {
#   block_public_acls   = true
#   block_public_policy = true
#   ignore_public_acls  = true
#   restrict_public_buckets = true
#   # account_id = data.aws_caller_identity.current.account_id # Optional if provider is configured for the target account
# }


# Optional: Remediation Configuration
# This requires an SSM document for remediation (e.g., AWS-DisableS3BucketPublicReadWrite).
#
# resource "aws_config_remediation_configuration" "s3_public_read_remediation" {
#   config_rule_name = aws_config_config_rule.s3_bucket_public_read_prohibited.name
#   target_type      = "SSM_DOCUMENT"
#   target_id        = "AWS-DisableS3BucketPublicReadWrite" # Example SSM document
#   target_version   = "1" # Specify version if needed
#
#   automatic = false # Set to true for automatic remediation
#   # maximum_automatic_attempts = 5
#   # retry_attempt_seconds    = 600
#
#   parameter {
#     name = "AutomationAssumeRole"
#     # resource_value = "arn:aws:iam::ACCOUNT_ID:role/your-config-remediation-role" # ARN of role Config can assume
#     # This role needs s3:PutBucketPublicAccessBlock and s3:GetBucketAcl, s3:GetBucketPolicy, s3:PutBucketAcl, s3:PutBucketPolicy permissions
#   }
#
#   parameter {
#     name           = "S3BucketName"
#     resource_value = "RESOURCE_ID" # Special value indicating to use the non-compliant resource's ID
#   }
# }