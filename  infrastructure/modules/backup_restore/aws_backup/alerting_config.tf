# Description: Configures alerts for AWS Backup job failures.
# Purpose: Notify administrators promptly of AWS Backup job failures or issues.
# Requirements: REQ-17-008

variable "backup_failure_sns_topic_arn" {
  description = "ARN of the SNS topic to send AWS Backup failure notifications to."
  type        = string
}

variable "aws_region" {
  description = "AWS region where the resources are deployed."
  type        = string
}

variable "tags" {
  description = "A map of tags to assign to the resources."
  type        = map(string)
  default     = {}
}

variable "event_bus_name" {
  description = "The name of the event bus to use. Defaults to the default event bus."
  type        = string
  default     = "default" # AWS default event bus
}

# IAM Role for EventBridge to publish to SNS topic
data "aws_iam_policy_document" "eventbridge_sns_publish_policy_doc" {
  statement {
    actions   = ["sns:Publish"]
    resources = [var.backup_failure_sns_topic_arn]
    effect    = "Allow"
  }
}

resource "aws_iam_role" "eventbridge_to_sns_role_backup_failure" {
  name_prefix        = "EventBridgeBackupFailureRole-"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Effect    = "Allow",
      Principal = {
        Service = "events.amazonaws.com"
      }
    }]
  })
  tags = merge(var.tags, { Name = "EventBridgeBackupFailureRole" })
}

resource "aws_iam_policy" "eventbridge_sns_publish_policy_backup_failure" {
  name_prefix = "EventBridgeSnsPublishBackupPolicy-"
  policy      = data.aws_iam_policy_document.eventbridge_sns_publish_policy_doc.json
  tags        = merge(var.tags, { Name = "EventBridgeSnsPublishBackupPolicy" })
}

resource "aws_iam_role_policy_attachment" "eventbridge_sns_publish_attach_backup_failure" {
  role       = aws_iam_role.eventbridge_to_sns_role_backup_failure.name
  policy_arn = aws_iam_policy.eventbridge_sns_publish_policy_backup_failure.arn
}


# EventBridge rule for Backup Job Failures
resource "aws_cloudwatch_event_rule" "backup_job_failed_rule" {
  name          = "AWSBackupJobFailedRule"
  description   = "Alert on AWS Backup job failures (FAILED, EXPIRED, ABORTED, INCOMPLETE states)"
  event_bus_name = var.event_bus_name
  event_pattern = jsonencode({
    "source" : ["aws.backup"],
    "detail-type" : ["Backup Job State Change"],
    "detail" : {
      "state" : [
        "FAILED",
        "EXPIRED", # A backup job could expire if it doesn't complete within its window
        "ABORTED",
        "INCOMPLETE" # Jobs that did not complete successfully
      ]
    }
  })
  tags = merge(var.tags, { Name = "AWSBackupJobFailedRule" })
}

resource "aws_cloudwatch_event_target" "backup_job_failed_sns_target" {
  rule      = aws_cloudwatch_event_rule.backup_job_failed_rule.name
  target_id = "SendToBackupFailureSNS"
  arn       = var.backup_failure_sns_topic_arn
  role_arn  = aws_iam_role.eventbridge_to_sns_role_backup_failure.arn # Required if target is SNS and rule is in different account or needs specific perms
  event_bus_name = var.event_bus_name
}

# EventBridge rule for Restore Job Failures
resource "aws_cloudwatch_event_rule" "restore_job_failed_rule" {
  name          = "AWSRestoreJobFailedRule"
  description   = "Alert on AWS Backup restore job failures"
  event_bus_name = var.event_bus_name
  event_pattern = jsonencode({
    "source" : ["aws.backup"],
    "detail-type" : ["Restore Job State Change"],
    "detail" : {
      "status" : [ # Note: 'status' for Restore Job, 'state' for Backup Job
        "FAILED",
        "ABORTED",
        "INCOMPLETE"
      ]
    }
  })
  tags = merge(var.tags, { Name = "AWSRestoreJobFailedRule" })
}

resource "aws_cloudwatch_event_target" "restore_job_failed_sns_target" {
  rule      = aws_cloudwatch_event_rule.restore_job_failed_rule.name
  target_id = "SendRestoreFailureToSNS"
  arn       = var.backup_failure_sns_topic_arn
  role_arn  = aws_iam_role.eventbridge_to_sns_role_backup_failure.arn
  event_bus_name = var.event_bus_name
}

# Optional: Rule for Copy Job Failures
resource "aws_cloudwatch_event_rule" "copy_job_failed_rule" {
  name          = "AWSCopyJobFailedRule"
  description   = "Alert on AWS Backup copy job failures"
  event_bus_name = var.event_bus_name
  event_pattern = jsonencode({
    "source" : ["aws.backup"],
    "detail-type" : ["Copy Job State Change"],
    "detail" : {
      "state" : [
        "FAILED",
        "ABORTED",
        "INCOMPLETE"
      ]
    }
  })
  tags = merge(var.tags, { Name = "AWSCopyJobFailedRule" })
}

resource "aws_cloudwatch_event_target" "copy_job_failed_sns_target" {
  rule      = aws_cloudwatch_event_rule.copy_job_failed_rule.name
  target_id = "SendCopyFailureToSNS"
  arn       = var.backup_failure_sns_topic_arn
  role_arn  = aws_iam_role.eventbridge_to_sns_role_backup_failure.arn
  event_bus_name = var.event_bus_name
}