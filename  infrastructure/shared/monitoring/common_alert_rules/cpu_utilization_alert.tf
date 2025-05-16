# Purpose: Reusable alert definition for high CPU utilization across various AWS resources.
# LogicDescription: Creates a generic AWS CloudWatch alarm resource.
# This file acts as the main.tf for a mini-module for this specific alert.
# Requirement: REQ-17-007

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
}

variable "alarm_name_prefix" {
  description = "Prefix for the alarm name. Full name will be <prefix>-cpu-utilization-high."
  type        = string
}

variable "alarm_description" {
  description = "Description for the CloudWatch alarm."
  type        = string
  default     = "CPU utilization is high."
}

variable "metric_namespace" {
  description = "The namespace for the CloudWatch metric (e.g., AWS/EC2, AWS/RDS)."
  type        = string
}

variable "metric_name" {
  description = "The name of the CloudWatch metric."
  type        = string
  default     = "CPUUtilization"
}

variable "dimensions" {
  description = "A map of dimensions for the CloudWatch metric (e.g., { InstanceId = \"i-123\" })."
  type        = map(string)
}

variable "threshold" {
  description = "The value against which the specified statistic is compared."
  type        = number
  default     = 80 # e.g., 80%
}

variable "period" {
  description = "The period in seconds over which the specified statistic is applied."
  type        = number
  default     = 300 # 5 minutes
}

variable "evaluation_periods" {
  description = "The number of periods over which data is compared to the specified threshold."
  type        = number
  default     = 2
}

variable "statistic" {
  description = "The statistic to apply to the alarm's metric data."
  type        = string
  default     = "Average" # Could be Maximum, Minimum, SampleCount, Sum
}

variable "comparison_operator" {
  description = "The arithmetic operation to use when comparing the specified statistic and threshold."
  type        = string
  default     = "GreaterThanOrEqualToThreshold"
}

variable "alarm_actions" {
  description = "A list of ARNs to notify when the alarm transitions to the ALARM state."
  type        = list(string)
  default     = []
}

variable "ok_actions" {
  description = "A list of ARNs to notify when the alarm transitions to the OK state."
  type        = list(string)
  default     = []
}

variable "insufficient_data_actions" {
  description = "A list of ARNs to notify when the alarm transitions to the INSUFFICIENT_DATA state."
  type        = list(string)
  default     = []
}

variable "treat_missing_data" {
  description = "Sets how this alarm is to handle missing data points. Valid values are breaching, notBreaching, ignore, and missing."
  type        = string
  default     = "missing" # Common default
}

variable "tags" {
  description = "A map of tags to assign to the alarm."
  type        = map(string)
  default     = {}
}

resource "aws_cloudwatch_metric_alarm" "cpu_utilization_high" {
  alarm_name          = "${var.alarm_name_prefix}-cpu-utilization-high"
  alarm_description   = var.alarm_description
  comparison_operator = var.comparison_operator
  evaluation_periods  = var.evaluation_periods
  metric_name         = var.metric_name
  namespace           = var.metric_namespace
  period              = var.period
  statistic           = var.statistic
  threshold           = var.threshold
  dimensions          = var.dimensions

  alarm_actions                = var.alarm_actions
  ok_actions                   = var.ok_actions
  insufficient_data_actions    = var.insufficient_data_actions
  treat_missing_data           = var.treat_missing_data

  tags = var.tags
}

output "alarm_arn" {
  description = "The ARN of the created CloudWatch alarm."
  value       = aws_cloudwatch_metric_alarm.cpu_utilization_high.arn
}

output "alarm_id" {
  description = "The ID of the created CloudWatch alarm."
  value       = aws_cloudwatch_metric_alarm.cpu_utilization_high.id
}