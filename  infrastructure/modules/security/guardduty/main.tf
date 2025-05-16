# Purpose: Enable AWS GuardDuty for continuous security monitoring and threat detection.
# LogicDescription: Enables GuardDuty detector. Configures S3 export for findings.
# Sets up EventBridge rules to capture findings and trigger SNS notifications.
# Requirement: REQ-17-008

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
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
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-guardduty-detector"
    }
  )
}

# Optional: S3 bucket for publishing findings
resource "aws_s3_bucket" "guardduty_findings_export_bucket" {
  count = var.export_findings_to_s3 ? 1 : 0

  bucket        = "${var.project_name}-${var.environment}-guardduty-findings-${data.aws_caller_identity.current.account_id}"
  force_destroy = var.s3_force_destroy # Set to true for non-prod to allow easier cleanup

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-guardduty-findings-bucket"
    }
  )
}

resource "aws_s3_bucket_policy" "guardduty_findings_export_bucket_policy" {
  count = var.export_findings_to_s3 ? 1 : 0

  bucket = aws_s3_bucket.guardduty_findings_export_bucket[0].id
  policy = data.aws_iam_policy_document.guardduty_s3_export_policy[0].json
}

data "aws_iam_policy_document" "guardduty_s3_export_policy" {
  count = var.export_findings_to_s3 ? 1 : 0
  statement {
    sid    = "AllowGuardDutyWrite"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["guardduty.amazonaws.com"]
    }
    actions   = ["s3:PutObject"]
    resources = ["${aws_s3_bucket.guardduty_findings_export_bucket[0].arn}/*"]
    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
  }
}

resource "aws_guardduty_publishing_destination" "s3_export" {
  count = var.export_findings_to_s3 ? 1 : 0

  detector_id     = aws_guardduty_detector.main.id
  destination_arn = aws_s3_bucket.guardduty_findings_export_bucket[0].arn
  kms_key_arn     = var.s3_export_kms_key_arn # KMS key for encrypting findings in S3

  destination_type = "S3" # Default, explicitly stated

  depends_on = [aws_s3_bucket_policy.guardduty_findings_export_bucket_policy]
}


# EventBridge Rule for GuardDuty Findings
resource "aws_cloudwatch_event_rule" "guardduty_findings_event_rule" {
  # Using aws_cloudwatch_event_rule for broader compatibility, can be aws_events_rule
  count = var.alert_on_findings && var.sns_topic_arn_for_alerts != "" ? 1 : 0

  name        = "${var.project_name}-${var.environment}-guardduty-findings-rule"
  description = "Capture GuardDuty findings and send to SNS"

  event_pattern = jsonencode({
    "source" : ["aws.guardduty"],
    "detail-type" : ["GuardDuty Finding"],
    "detail" : {
      "severity" : var.alert_min_severity_levels # e.g. [4, 4.0, 5, 5.0, 6, 6.0, 7, 7.0, 8, 8.0, 8.9] for Medium and High
    }
  })

  tags = var.common_tags
}

resource "aws_cloudwatch_event_target" "sns_target_for_guardduty" {
  count = var.alert_on_findings && var.sns_topic_arn_for_alerts != "" ? 1 : 0

  rule      = aws_cloudwatch_event_rule.guardduty_findings_event_rule[0].name
  target_id = "${var.project_name}-${var.environment}-guardduty-sns-target"
  arn       = var.sns_topic_arn_for_alerts

  # Input transformer can be used to format the message sent to SNS
  # input_transformer {
  #   input_paths = {
  #     "severity" = "$.detail.severity",
  #     "title"    = "$.detail.title",
  #     "region"   = "$.region"
  #   }
  #   input_template = "\"GuardDuty Finding in <region>: [<severity>] <title>\""
  # }
}

# IAM Role for EventBridge to publish to SNS (if SNS topic policy doesn't allow events.amazonaws.com)
# This is often not needed if the SNS topic policy grants `events.amazonaws.com` publish permissions.
# data "aws_iam_policy_document" "eventbridge_sns_publish_policy" {
#   count = var.alert_on_findings && var.sns_topic_arn_for_alerts != "" && var.create_eventbridge_iam_role ? 1 : 0
#   statement {
#     actions   = ["sns:Publish"]
#     resources = [var.sns_topic_arn_for_alerts]
#   }
# }
# resource "aws_iam_role" "eventbridge_sns_role" {
#   count = var.alert_on_findings && var.sns_topic_arn_for_alerts != "" && var.create_eventbridge_iam_role ? 1 : 0
#   name  = "${var.project_name}-${var.environment}-eventbridge-sns-role"
#   assume_role_policy = jsonencode({
#     Version = "2012-10-17",
#     Statement = [{
#       Action    = "sts:AssumeRole",
#       Effect    = "Allow",
#       Principal = { Service = "events.amazonaws.com" }
#     }]
#   })
# }
# resource "aws_iam_role_policy" "eventbridge_sns_role_policy" {
#   count = var.alert_on_findings && var.sns_topic_arn_for_alerts != "" && var.create_eventbridge_iam_role ? 1 : 0
#   name  = "${var.project_name}-${var.environment}-eventbridge-sns-policy"
#   role  = aws_iam_role.eventbridge_sns_role[0].id
#   policy = data.aws_iam_policy_document.eventbridge_sns_publish_policy[0].json
# }
# And then assign `role_arn = aws_iam_role.eventbridge_sns_role[0].arn` to `aws_cloudwatch_event_target`

data "aws_caller_identity" "current" {}