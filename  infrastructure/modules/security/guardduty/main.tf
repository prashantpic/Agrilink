# Purpose: Enable AWS GuardDuty for continuous security monitoring and threat detection.
# LogicDescription: Enables GuardDuty detector, configures S3 export for findings,
# and sets up CloudWatch/EventBridge rules to capture findings and trigger notifications.
# ImplementedFeatures: GuardDuty Enablement, S3 Export for Findings, Alerting for GuardDuty Findings.
# RequirementIds: REQ-17-008

locals {
  detector_id = aws_guardduty_detector.main.id
  tags        = var.tags
}

resource "aws_guardduty_detector" "main" {
  enable                       = var.enable_guardduty
  finding_publishing_frequency = var.finding_publishing_frequency # e.g., FIFTEEN_MINUTES, ONE_HOUR, SIX_HOURS

  datasources {
    s3_logs {
      enable = var.enable_s3_protection
    }
    kubernetes {
      audit_logs {
        enable = var.enable_eks_protection
      }
    }
    malware_protection {
      scan_ec2_instance_with_findings {
        ebs_volumes {
          enable = var.enable_malware_protection_ebs
        }
      }
    }
    # Add other data sources as needed (e.g., RDS Protection when it becomes GA and supported by Terraform)
  }
  
  tags = merge(local.tags, {
    Name = "${var.project_name}-${var.environment}-guardduty-detector"
  })
}

resource "aws_s3_bucket" "guardduty_findings_export_bucket" {
  count = var.export_findings_to_s3 ? 1 : 0

  bucket = "${var.project_name}-${var.environment}-guardduty-findings-${data.aws_caller_identity.current.account_id}"
  # acl    = "private" # Default is private, explicitly setting for clarity

  # It's crucial to set a bucket policy that allows GuardDuty to write to this bucket.
  # Server-side encryption should be enabled.
  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm     = "AES256"
      }
    }
  }

  versioning {
    enabled = true
  }

  lifecycle_rule {
    enabled = true
    id      = "findings-retention"
    expiration {
      days = var.s3_findings_retention_days
    }
    # transition { # Optionally transition to Glacier
    #   days          = 90
    #   storage_class = "GLACIER"
    # }
  }
  
  # Block public access
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true

  tags = merge(local.tags, {
    Name    = "${var.project_name}-${var.environment}-guardduty-findings-bucket",
    Purpose = "GuardDuty Findings Export"
  })
}

data "aws_iam_policy_document" "guardduty_findings_s3_bucket_policy_doc" {
  count = var.export_findings_to_s3 ? 1 : 0

  statement {
    sid    = "AllowGuardDutyToWrite"
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetBucketLocation" # Required by GuardDuty
    ]
    resources = [
      aws_s3_bucket.guardduty_findings_export_bucket[0].arn,
      "${aws_s3_bucket.guardduty_findings_export_bucket[0].arn}/*",
    ]
    principals {
      type        = "Service"
      identifiers = ["guardduty.amazonaws.com"]
    }
    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
     condition {
      test     = "ArnLike"
      variable = "aws:SourceArn"
      values   = ["arn:aws:guardduty:${var.aws_region}:${data.aws_caller_identity.current.account_id}:detector/${local.detector_id}"]
    }
  }
}

resource "aws_s3_bucket_policy" "guardduty_findings_export_bucket_policy" {
  count  = var.export_findings_to_s3 ? 1 : 0
  bucket = aws_s3_bucket.guardduty_findings_export_bucket[0].id
  policy = data.aws_iam_policy_document.guardduty_findings_s3_bucket_policy_doc[0].json
}


resource "aws_guardduty_publishing_destination" "main" {
  count           = var.export_findings_to_s3 ? 1 : 0
  detector_id     = local.detector_id
  destination_arn = aws_s3_bucket.guardduty_findings_export_bucket[0].arn
  kms_key_arn     = var.s3_findings_kms_key_arn # KMS key for GuardDuty to use when writing to S3

  depends_on = [aws_s3_bucket_policy.guardduty_findings_export_bucket_policy]
}


# REQ-17-008: Alerting for security findings
resource "aws_cloudwatch_event_rule" "guardduty_findings_event_rule" {
  count = var.alert_on_findings ? 1 : 0

  name        = "${var.project_name}-${var.environment}-guardduty-findings-rule"
  description = "Capture GuardDuty findings and send to SNS"

  event_pattern = jsonencode({
    source      = ["aws.guardduty"],
    detail-type = ["GuardDuty Finding"],
    detail = {
      severity = var.alert_finding_severities # e.g. [4, 4.0, 7.0, 8.9] for Medium to High
    }
  })

  tags = merge(local.tags, {
    Name = "${var.project_name}-${var.environment}-guardduty-findings-rule"
  })
}

resource "aws_cloudwatch_event_target" "guardduty_findings_sns_target" {
  count = var.alert_on_findings ? 1 : 0

  rule      = aws_cloudwatch_event_rule.guardduty_findings_event_rule[0].name
  target_id = "${var.project_name}-${var.environment}-guardduty-sns-target"
  arn       = var.sns_topic_arn_for_alerts # ARN of the SNS topic (e.g., from sns_topics module)

  # Input transformer to customize the notification message (optional)
  # input_transformer {
  #   input_paths = {
  #     "severity": "$.detail.severity",
  #     "title": "$.detail.title",
  #     "description": "$.detail.description",
  #     "id": "$.detail.id",
  #     "region": "$.region"
  #   }
  #   input_template = "\"GuardDuty Finding in ${var.aws_region}: [Severity <severity>] <title>. Description: <description>. Finding ID: <id>\""
  # }
}

data "aws_caller_identity" "current" {} # Already defined above, ensure it's unique if multiple data blocks

# If GuardDuty needs to publish to SNS topic directly, an IAM role for EventBridge might be needed
# if the SNS topic policy does not grant "events.amazonaws.com" publish permission.
# However, aws_cloudwatch_event_target usually handles this if the SNS topic policy is permissive enough
# or if a resource-based policy is automatically created. Explicit role for clarity:

resource "aws_iam_role" "eventbridge_to_sns_role" {
  count = var.alert_on_findings && var.create_eventbridge_sns_role ? 1 : 0
  name  = "${var.project_name}-${var.environment}-eventbridge-sns-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Effect    = "Allow",
      Principal = { Service = "events.amazonaws.com" }
    }]
  })
  tags = local.tags
}

resource "aws_iam_policy" "eventbridge_sns_publish_policy" {
  count = var.alert_on_findings && var.create_eventbridge_sns_role ? 1 : 0
  name  = "${var.project_name}-${var.environment}-eventbridge-sns-publish-policy"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action   = "sns:Publish",
      Effect   = "Allow",
      Resource = var.sns_topic_arn_for_alerts
    }]
  })
  tags = local.tags
}

resource "aws_iam_role_policy_attachment" "eventbridge_sns_publish_attachment" {
  count      = var.alert_on_findings && var.create_eventbridge_sns_role ? 1 : 0
  role       = aws_iam_role.eventbridge_to_sns_role[0].name
  policy_arn = aws_iam_policy.eventbridge_sns_publish_policy[0].arn
}

# If using the role, assign it to the event target
# resource "aws_cloudwatch_event_target" "guardduty_findings_sns_target_with_role" {
#   count = var.alert_on_findings && var.create_eventbridge_sns_role ? 1 : 0
#   ...
#   role_arn = aws_iam_role.eventbridge_to_sns_role[0].arn
# }