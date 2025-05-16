# Purpose: Reusable alert definition for high CPU utilization across various AWS resources.
# LogicDescription: Creates a generic AWS CloudWatch alarm resource definition.
# This resource is defined with variables for 'metric_name', 'namespace', 'dimensions',
# 'threshold', 'period', 'evaluation_periods', and 'alarm_actions'.
# ImplementedFeatures: CPU Utilization Alarm Template
# RequirementIds: REQ-17-007

# This file defines a single aws_cloudwatch_metric_alarm resource.
# It's a template and expects variables to be defined in the calling scope.
# Variables expected:
# - var.alarm_name_prefix (string)
# - var.cpu_threshold (number)
# - var.cpu_evaluation_periods (number)
# - var.cpu_period_seconds (number)
# - var.alarm_sns_topic_arns (list of strings)
# - var.alarm_dimensions (map of strings)
# - var.alarm_namespace (string, e.g., "AWS/EC2", "AWS/RDS")
# - var.alarm_resource_id (string, for naming and tagging)
# - var.tags (map of strings, for common tags)

resource "aws_cloudwatch_metric_alarm" "high_cpu_utilization" {
  alarm_name          = "${var.alarm_name_prefix}-high-cpu-${var.alarm_resource_id}"
  alarm_description   = "High CPU utilization detected for ${var.alarm_resource_id} in namespace ${var.alarm_namespace}"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = var.cpu_evaluation_periods
  metric_name         = "CPUUtilization"
  namespace           = var.alarm_namespace # e.g., "AWS/EC2", "AWS/RDS", "AWS/ECS"
  period              = var.cpu_period_seconds
  statistic           = "Average"
  threshold           = var.cpu_threshold
  treat_missing_data  = "missing" # or "breaching", "notBreaching", "ignore"

  dimensions = var.alarm_dimensions # e.g., { InstanceId = "i-12345" } or { DBInstanceIdentifier = "mydb" }

  alarm_actions = var.alarm_sns_topic_arns # List of SNS topic ARNs
  ok_actions    = var.alarm_sns_topic_arns # Optionally notify on OK state

  tags = merge(
    var.tags,
    {
      Name        = "${var.alarm_name_prefix}-high-cpu-${var.alarm_resource_id}"
      AlertType   = "CPUUtilization"
      ResourceId  = var.alarm_resource_id
      Environment = var.environment # Assuming var.environment is available
      Severity    = "Critical" # Or make this a variable
    }
  )
}

# Example of how this might be "called" or used in a module (conceptual):
#
# module "ec2_instance_alerts" {
#   source = "./some_module_that_uses_this_template" # Not a direct call
#
#   alarm_name_prefix       = "${var.project_name}-${var.environment}"
#   cpu_threshold           = 80
#   cpu_evaluation_periods  = 3
#   cpu_period_seconds      = 300
#   alarm_sns_topic_arns    = [module.sns_topics.critical_alerts_topic_arn]
#   alarm_dimensions        = { InstanceId = aws_instance.my_app.id }
#   alarm_namespace         = "AWS/EC2"
#   alarm_resource_id       = aws_instance.my_app.id
#   tags                    = local.tags
#   environment             = var.environment
# }
#
# Where `some_module_that_uses_this_template` would effectively inline the resource block above,
# passing its own variables to it.
#
# If this file itself were a module, it would have its own variables.tf:
# variable "alarm_name_prefix" {} ... etc.
# And its main.tf would contain the resource block above, using `var.<variable_name>`.
# However, the file structure only lists this single .tf file.