# Purpose: Notify administrators promptly of AWS Backup job failures or issues.
# LogicDescription: Sets up AWS CloudWatch Event rules (or EventBridge rules) to capture events
# related to AWS Backup job status and trigger notifications to an SNS topic.
# REQ-17-008: Alert on backup job failures.

variable "backup_failure_sns_topic_arn" {
  description = "ARN of the SNS topic to send backup failure notifications to."
  type        = string
}

variable "aws_region" {
  description = "AWS Region where the backup events occur."
  type        = string
}

variable "tags" {
  description = "A map of tags to add to all resources."
  type        = map(string)
  default     = {}
}

locals {
  event_states_to_alert = ["FAILED", "EXPIRED", "ABORTED", "TIMED_OUT"] # Added TIMED_OUT as another failure state
}

data "aws_caller_identity" "current" {}
data "aws_partition" "current" {}

# IAM Role for EventBridge to publish to SNS
resource "aws_iam_role" "backup_event_sns_publish_role" {
  name = "BackupEventToSNSPublishRole"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = {
        Service = "events.amazonaws.com"
      }
    }]
  })
  tags = var.tags
}

resource "aws_iam_policy" "backup_event_sns_publish_policy" {
  name   = "BackupEventSNSPublishPolicy"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action   = "sns:Publish",
      Effect   = "Allow",
      Resource = var.backup_failure_sns_topic_arn
    }]
  })
  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "backup_event_sns_publish_attach" {
  role       = aws_iam_role.backup_event_sns_publish_role.name
  policy_arn = aws_iam_policy.backup_event_sns_publish_policy.arn
}

# EventBridge Rule for Backup Job State Changes
resource "aws_cloudwatch_event_rule" "backup_job_state_change_rule" {
  name        = "AWSBackupJobStateChangeAlertRule"
  description = "Captures AWS Backup job state changes for alerting."
  event_pattern = jsonencode({
    "source" : ["aws.backup"],
    "detail-type" : ["Backup Job State Change"],
    "detail" : {
      "state" : local.event_states_to_alert
    }
  })
  tags = var.tags
}

resource "aws_cloudwatch_event_target" "backup_job_state_change_target" {
  rule      = aws_cloudwatch_event_rule.backup_job_state_change_rule.name
  target_id = "SendToBackupFailureSNS"
  arn       = var.backup_failure_sns_topic_arn
  role_arn  = aws_iam_role.backup_event_sns_publish_role.arn # Required if target needs permissions beyond basic SNS publish

  # Input transformer to make the notification more readable
  input_transformer {
    input_paths = {
      "awsRegion"        = "$.awsRegion",
      "backupJobId"      = "$.detail.backupJobId",
      "backupVaultName"  = "$.detail.backupVaultName",
      "resourceArn"      = "$.detail.resourceArn",
      "resourceType"     = "$.detail.resourceType",
      "state"            = "$.detail.state",
      "statusMessage"    = "$.detail.statusMessage",
      "time"             = "$.time"
    }
    input_template = <<TEMPLATE
"AWS Backup Job Notification"
"Time: <time>"
"Region: <awsRegion>"
"Status: <state>"
"Backup Job ID: <backupJobId>"
"Backup Vault: <backupVaultName>"
"Resource ARN: <resourceArn>"
"Resource Type: <resourceType>"
"Message: <statusMessage>"
"Link to Backup Job: https://<awsRegion>.console.aws.amazon.com/backup/home?region=<awsRegion>#backup-job/<backupJobId>"
TEMPLATE
  }
}

# EventBridge Rule for Restore Job State Changes
resource "aws_cloudwatch_event_rule" "restore_job_state_change_rule" {
  name        = "AWSRestoreJobStateChangeAlertRule"
  description = "Captures AWS Backup restore job state changes for alerting."
  event_pattern = jsonencode({
    "source" : ["aws.backup"],
    "detail-type" : ["Restore Job State Change"],
    "detail" : {
      "status" : local.event_states_to_alert # Restore jobs use 'status' instead of 'state'
    }
  })
  tags = var.tags
}

resource "aws_cloudwatch_event_target" "restore_job_state_change_target" {
  rule      = aws_cloudwatch_event_rule.restore_job_state_change_rule.name
  target_id = "SendToRestoreFailureSNS"
  arn       = var.backup_failure_sns_topic_arn
  role_arn  = aws_iam_role.backup_event_sns_publish_role.arn # Required if target needs permissions

  # Input transformer to make the notification more readable
  input_transformer {
    input_paths = {
      "awsRegion"        = "$.awsRegion",
      "restoreJobId"     = "$.detail.restoreJobId",
      "resourceType"     = "$.detail.resourceType",
      "status"           = "$.detail.status",
      "statusMessage"    = "$.detail.statusMessage",
      "time"             = "$.time"
    }
    input_template = <<TEMPLATE
"AWS Restore Job Notification"
"Time: <time>"
"Region: <awsRegion>"
"Status: <status>"
"Restore Job ID: <restoreJobId>"
"Resource Type: <resourceType>"
"Message: <statusMessage>"
"Link to Restore Job: https://<awsRegion>.console.aws.amazon.com/backup/home?region=<awsRegion>#restore-job/<restoreJobId>"
TEMPLATE
  }
}