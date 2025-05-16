# Purpose: Monitor database performance and resource utilization, and alert on critical issues for RDS/Aurora instances.
# LogicDescription: Creates AWS CloudWatch alarms for key RDS/Aurora metrics.
# REQ-17-007: Alert conditions for database performance and utilization.

variable "db_instance_identifier" {
  description = "The identifier of the RDS instance. For Aurora, this is one of the instance identifiers in the cluster."
  type        = string
  default     = null # Either db_instance_identifier or db_cluster_identifier must be set
}

variable "db_cluster_identifier" {
  description = "The identifier of the RDS Aurora cluster. Used for cluster-level metrics."
  type        = string
  default     = null # Either db_instance_identifier or db_cluster_identifier must be set
}

variable "alarm_sns_topic_arns" {
  description = "A list of SNS topic ARNs to send alarm notifications to."
  type        = list(string)
}

variable "cpu_utilization_threshold" {
  description = "CPU utilization threshold for alerting (percentage)."
  type        = number
  default     = 80
}

variable "freeable_memory_threshold_gb" {
  description = "Freeable memory threshold for alerting (in GB). Converts to bytes for alarm."
  type        = number
  default     = 1 # Alert if less than 1GB freeable memory
}

variable "free_storage_space_threshold_gb" {
  description = "Free storage space threshold for alerting (in GB). Converts to bytes for alarm."
  type        = number
  default     = 10 # Alert if less than 10GB free storage
}

variable "database_connections_threshold" {
  description = "Database connections threshold for alerting."
  type        = number
  default     = 500
}

variable "read_latency_threshold_ms" {
  description = "Read latency threshold for alerting (in milliseconds)."
  type        = number
  default     = 100
}

variable "write_latency_threshold_ms" {
  description = "Write latency threshold for alerting (in milliseconds)."
  type        = number
  default     = 100
}

variable "replica_lag_threshold_seconds" {
  description = "Replica lag threshold for alerting (in seconds). Applicable for Aurora clusters or RDS read replicas."
  type        = number
  default     = 300 # 5 minutes
}

variable "evaluation_periods" {
  description = "The number of periods over which data is compared to the specified threshold."
  type        = number
  default     = 3
}

variable "period_seconds" {
  description = "The period in seconds over which the specified statistic is applied."
  type        = number
  default     = 300 # 5 minutes
}

variable "tags" {
  description = "A map of tags to add to all alarm resources."
  type        = map(string)
  default     = {}
}

locals {
  # Determine if we are dealing with a cluster or a standalone instance for metric dimensions
  is_cluster_metric    = var.db_cluster_identifier != null
  instance_metric_id   = var.db_instance_identifier != null ? var.db_instance_identifier : (var.db_cluster_identifier != null ? "${var.db_cluster_identifier}-primary" : null) # Fallback if only cluster id is provided for instance metrics
  instance_dimensions = var.db_instance_identifier != null ? { DBInstanceIdentifier = var.db_instance_identifier } : null
  cluster_dimensions  = var.db_cluster_identifier != null ? { DBClusterIdentifier = var.db_cluster_identifier } : null
}

# CPU Utilization Alarm
resource "aws_cloudwatch_metric_alarm" "rds_cpu_utilization" {
  count               = local.instance_dimensions != null ? 1 : 0
  alarm_name          = "${local.instance_metric_id}-cpu-utilization-high"
  alarm_description   = "RDS instance ${local.instance_metric_id} CPU utilization is high."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = var.evaluation_periods
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = var.period_seconds
  statistic           = "Average"
  threshold           = var.cpu_utilization_threshold
  dimensions          = local.instance_dimensions
  alarm_actions       = var.alarm_sns_topic_arns
  ok_actions          = var.alarm_sns_topic_arns
  tags                = var.tags
}

# Freeable Memory Alarm
resource "aws_cloudwatch_metric_alarm" "rds_freeable_memory" {
  count               = local.instance_dimensions != null ? 1 : 0
  alarm_name          = "${local.instance_metric_id}-freeable-memory-low"
  alarm_description   = "RDS instance ${local.instance_metric_id} freeable memory is low."
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = var.evaluation_periods
  metric_name         = "FreeableMemory"
  namespace           = "AWS/RDS"
  period              = var.period_seconds
  statistic           = "Average"
  threshold           = var.freeable_memory_threshold_gb * 1024 * 1024 * 1024 # Convert GB to Bytes
  dimensions          = local.instance_dimensions
  alarm_actions       = var.alarm_sns_topic_arns
  ok_actions          = var.alarm_sns_topic_arns
  tags                = var.tags
}

# Free Storage Space Alarm
resource "aws_cloudwatch_metric_alarm" "rds_free_storage_space" {
  count               = local.instance_dimensions != null ? 1 : 0
  alarm_name          = "${local.instance_metric_id}-free-storage-space-low"
  alarm_description   = "RDS instance ${local.instance_metric_id} free storage space is low."
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = var.evaluation_periods
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = var.period_seconds
  statistic           = "Average"
  threshold           = var.free_storage_space_threshold_gb * 1024 * 1024 * 1024 # Convert GB to Bytes
  dimensions          = local.instance_dimensions
  alarm_actions       = var.alarm_sns_topic_arns
  ok_actions          = var.alarm_sns_topic_arns
  tags                = var.tags
}

# Database Connections Alarm
resource "aws_cloudwatch_metric_alarm" "rds_database_connections" {
  count               = local.instance_dimensions != null ? 1 : 0
  alarm_name          = "${local.instance_metric_id}-database-connections-high"
  alarm_description   = "RDS instance ${local.instance_metric_id} database connections are high."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = var.evaluation_periods
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = var.period_seconds
  statistic           = "Average"
  threshold           = var.database_connections_threshold
  dimensions          = local.instance_dimensions
  alarm_actions       = var.alarm_sns_topic_arns
  ok_actions          = var.alarm_sns_topic_arns
  tags                = var.tags
}

# Read Latency Alarm
resource "aws_cloudwatch_metric_alarm" "rds_read_latency" {
  count               = local.instance_dimensions != null ? 1 : 0
  alarm_name          = "${local.instance_metric_id}-read-latency-high"
  alarm_description   = "RDS instance ${local.instance_metric_id} read latency is high."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = var.evaluation_periods
  metric_name         = "ReadLatency"
  namespace           = "AWS/RDS"
  period              = var.period_seconds
  statistic           = "Average"
  threshold           = var.read_latency_threshold_ms / 1000 # Convert ms to seconds
  unit                = "Seconds"
  dimensions          = local.instance_dimensions
  alarm_actions       = var.alarm_sns_topic_arns
  ok_actions          = var.alarm_sns_topic_arns
  tags                = var.tags
}

# Write Latency Alarm
resource "aws_cloudwatch_metric_alarm" "rds_write_latency" {
  count               = local.instance_dimensions != null ? 1 : 0
  alarm_name          = "${local.instance_metric_id}-write-latency-high"
  alarm_description   = "RDS instance ${local.instance_metric_id} write latency is high."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = var.evaluation_periods
  metric_name         = "WriteLatency"
  namespace           = "AWS/RDS"
  period              = var.period_seconds
  statistic           = "Average"
  threshold           = var.write_latency_threshold_ms / 1000 # Convert ms to seconds
  unit                = "Seconds"
  dimensions          = local.instance_dimensions
  alarm_actions       = var.alarm_sns_topic_arns
  ok_actions          = var.alarm_sns_topic_arns
  tags                = var.tags
}

# Replica Lag Alarm (for Aurora clusters or RDS read replicas)
resource "aws_cloudwatch_metric_alarm" "rds_replica_lag" {
  count               = local.cluster_dimensions != null ? 1 : 0 # Typically a cluster metric for Aurora
  alarm_name          = "${var.db_cluster_identifier}-replica-lag-high"
  alarm_description   = "RDS Aurora cluster ${var.db_cluster_identifier} replica lag is high."
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = var.evaluation_periods
  metric_name         = "AuroraReplicaLag" # Use "ReplicaLag" for non-Aurora RDS read replicas
  namespace           = "AWS/RDS"
  period              = var.period_seconds
  statistic           = "Maximum" # Use Maximum or Average depending on needs
  threshold           = var.replica_lag_threshold_seconds
  unit                = "Seconds"
  dimensions          = local.cluster_dimensions
  alarm_actions       = var.alarm_sns_topic_arns
  ok_actions          = var.alarm_sns_topic_arns
  tags                = var.tags
}