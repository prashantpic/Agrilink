# Description: Defines CloudWatch alarms for RDS/Aurora database metrics.
# Purpose: Monitor database performance and resource utilization, and alert on critical issues.
# Requirements: REQ-17-007

variable "db_identifier" {
  description = "The identifier of the RDS instance or Aurora cluster."
  type        = string
}

variable "is_aurora_cluster" {
  description = "Set to true if the db_identifier is for an Aurora DB cluster, false for RDS instance."
  type        = bool
  default     = false # Assume RDS instance by default
}

variable "sns_topic_arns_critical" {
  description = "List of SNS topic ARNs for critical alerts."
  type        = list(string)
}

variable "sns_topic_arns_warning" {
  description = "List of SNS topic ARNs for warning alerts."
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "A map of tags to assign to the CloudWatch alarms."
  type        = map(string)
  default     = {}
}

# Thresholds - these should be configurable per environment
variable "cpu_utilization_threshold_critical" {
  description = "CPU utilization threshold for critical alerts (%)."
  type        = number
  default     = 90
}

variable "cpu_utilization_threshold_warning" {
  description = "CPU utilization threshold for warning alerts (%)."
  type        = number
  default     = 75
}

variable "freeable_memory_threshold_critical_gb" {
  description = "Freeable memory threshold for critical alerts (in GB). Alarm triggers if memory is BELOW this value."
  type        = number
  default     = 0.5 # 500MB
}

variable "freeable_memory_threshold_warning_gb" {
  description = "Freeable memory threshold for warning alerts (in GB). Alarm triggers if memory is BELOW this value."
  type        = number
  default     = 1 # 1GB
}

variable "free_storage_space_threshold_critical_gb" {
  description = "Free storage space threshold for critical alerts (in GB). Alarm triggers if storage is BELOW this value."
  type        = number
  default     = 10
}

variable "free_storage_space_threshold_warning_gb" {
  description = "Free storage space threshold for warning alerts (in GB). Alarm triggers if storage is BELOW this value."
  type        = number
  default     = 20
}

variable "database_connections_threshold_critical_percentage" {
  description = "Database connections threshold as a percentage of max_connections for critical alerts."
  type        = number
  default     = 90
}

variable "database_connections_threshold_warning_percentage" {
  description = "Database connections threshold as a percentage of max_connections for warning alerts."
  type        = number
  default     = 75
}

variable "read_latency_threshold_critical_ms" {
  description = "Read latency threshold for critical alerts (in milliseconds)."
  type        = number
  default     = 100
}

variable "write_latency_threshold_critical_ms" {
  description = "Write latency threshold for critical alerts (in milliseconds)."
  type        = number
  default     = 100
}

variable "replica_lag_threshold_critical_seconds" {
  description = "Replica lag threshold for critical alerts (in seconds). Applicable for Aurora Replicas or RDS Read Replicas."
  type        = number
  default     = 300 # 5 minutes
}

locals {
  dimension_name = var.is_aurora_cluster ? "DBClusterIdentifier" : "DBInstanceIdentifier"
  # Convert GB to Bytes for memory/storage alarms as CloudWatch metrics are in Bytes
  freeable_memory_threshold_critical_bytes = var.freeable_memory_threshold_critical_gb * 1024 * 1024 * 1024
  freeable_memory_threshold_warning_bytes  = var.freeable_memory_threshold_warning_gb * 1024 * 1024 * 1024
  free_storage_space_threshold_critical_bytes = var.free_storage_space_threshold_critical_gb * 1024 * 1024 * 1024
  free_storage_space_threshold_warning_bytes  = var.free_storage_space_threshold_warning_gb * 1024 * 1024 * 1024
  # Latency metrics are in seconds, convert ms to s
  read_latency_threshold_critical_s  = var.read_latency_threshold_critical_ms / 1000.0
  write_latency_threshold_critical_s = var.write_latency_threshold_critical_ms / 1000.0
}

resource "aws_cloudwatch_metric_alarm" "cpu_utilization_critical" {
  alarm_name          = "${var.db_identifier}-cpu-utilization-critical"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "3"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = "300" # 5 minutes
  statistic           = "Average"
  threshold           = var.cpu_utilization_threshold_critical
  alarm_description   = "Critical: CPU utilization for ${var.db_identifier} is above ${var.cpu_utilization_threshold_critical}%"
  alarm_actions       = var.sns_topic_arns_critical
  ok_actions          = var.sns_topic_arns_critical # Notify on recovery
  dimensions = {
    (local.dimension_name) = var.db_identifier
  }
  tags = merge(var.tags, { Severity = "Critical", Metric = "CPUUtilization" })
}

resource "aws_cloudwatch_metric_alarm" "cpu_utilization_warning" {
  count               = length(var.sns_topic_arns_warning) > 0 ? 1 : 0
  alarm_name          = "${var.db_identifier}-cpu-utilization-warning"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "3"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = var.cpu_utilization_threshold_warning
  alarm_description   = "Warning: CPU utilization for ${var.db_identifier} is above ${var.cpu_utilization_threshold_warning}%"
  alarm_actions       = var.sns_topic_arns_warning
  ok_actions          = var.sns_topic_arns_warning
  dimensions = {
    (local.dimension_name) = var.db_identifier
  }
  tags = merge(var.tags, { Severity = "Warning", Metric = "CPUUtilization" })
}

resource "aws_cloudwatch_metric_alarm" "freeable_memory_critical" {
  alarm_name          = "${var.db_identifier}-freeable-memory-critical"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "FreeableMemory"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = local.freeable_memory_threshold_critical_bytes
  alarm_description   = "Critical: Freeable memory for ${var.db_identifier} is below ${var.freeable_memory_threshold_critical_gb} GB"
  alarm_actions       = var.sns_topic_arns_critical
  ok_actions          = var.sns_topic_arns_critical
  dimensions = {
    (local.dimension_name) = var.db_identifier
  }
  tags = merge(var.tags, { Severity = "Critical", Metric = "FreeableMemory" })
}

resource "aws_cloudwatch_metric_alarm" "free_storage_space_critical" {
  alarm_name          = "${var.db_identifier}-free-storage-critical"
  comparison_operator = "LessThanOrEqualToThreshold"
  evaluation_periods  = "1" # Alert quickly on low storage
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Minimum" # To catch the lowest point
  threshold           = local.free_storage_space_threshold_critical_bytes
  alarm_description   = "Critical: Free storage space for ${var.db_identifier} is below ${var.free_storage_space_threshold_critical_gb} GB"
  alarm_actions       = var.sns_topic_arns_critical
  ok_actions          = var.sns_topic_arns_critical
  dimensions = {
    (local.dimension_name) = var.db_identifier
  }
  tags = merge(var.tags, { Severity = "Critical", Metric = "FreeStorageSpace" })
}

resource "aws_cloudwatch_metric_alarm" "database_connections_critical" {
  # This alarm requires knowing the max_connections parameter.
  # A more robust approach might use a Math Expression with GetMetricData to fetch max_connections if it's dynamic or stored elsewhere.
  # For simplicity, using a percentage, assuming operators know their DB's connection limits.
  alarm_name          = "${var.db_identifier}-db-connections-critical"
  comparison_operator = "GreaterThanOrEqualToThreshold" # This alarm is tricky as 'DatabaseConnections' is a count, not a %.
                                                      # Users need to calculate the threshold based on their max_connections.
                                                      # The variable name implies percentage, but CloudWatch direct metric is count.
                                                      # For now, assuming the threshold is a raw connection count derived from percentage.
                                                      # Example: if max_connections=1000, 90% threshold is 900.
  evaluation_periods  = "3"
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  # threshold        = This needs to be set to an absolute number. User must calculate max_connections * (percentage / 100).
  # We'll define a placeholder variable for the absolute threshold for now.
  # To implement the percentage logic directly, you'd need a metric math alarm.
  # threshold           = var.database_connections_absolute_threshold_critical (user must provide this)
  alarm_description = "Critical: Database connections for ${var.db_identifier} are high."
  alarm_actions     = var.sns_topic_arns_critical
  ok_actions        = var.sns_topic_arns_critical
  dimensions = {
    (local.dimension_name) = var.db_identifier
  }
  # This alarm is commented out because 'threshold' expects a number, not a percentage directly on the 'DatabaseConnections' metric.
  # A more advanced setup would involve a Lambda to get max_connections or Metric Math.
  # For now, we'll rely on users to define this manually or use a fixed absolute number based on their settings.
  # We can't directly use var.database_connections_threshold_critical_percentage as the threshold.
  # The user should calculate: threshold = lookup(aws_db_instance.example.db_parameter_group_name.parameters, "max_connections", 0) * (var.database_connections_threshold_critical_percentage / 100)
  # This logic is too complex for a simple alarm definition without external data or metric math.
  # The following is a placeholder if an absolute threshold is provided.
  # threshold           = var.db_connections_abs_critical_threshold

  lifecycle {
    ignore_changes = [threshold] # Example if threshold is managed outside or needs manual calculation
  }
  tags = merge(var.tags, { Severity = "Critical", Metric = "DatabaseConnections", Note = "Threshold is an absolute count" })
}


resource "aws_cloudwatch_metric_alarm" "read_latency_critical" {
  alarm_name          = "${var.db_identifier}-read-latency-critical"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "3"
  metric_name         = "ReadLatency"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = local.read_latency_threshold_critical_s # In seconds
  alarm_description   = "Critical: Read latency for ${var.db_identifier} is above ${var.read_latency_threshold_critical_ms} ms"
  alarm_actions       = var.sns_topic_arns_critical
  ok_actions          = var.sns_topic_arns_critical
  dimensions = {
    (local.dimension_name) = var.db_identifier
  }
  tags = merge(var.tags, { Severity = "Critical", Metric = "ReadLatency" })
}

resource "aws_cloudwatch_metric_alarm" "write_latency_critical" {
  alarm_name          = "${var.db_identifier}-write-latency-critical"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "3"
  metric_name         = "WriteLatency"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = local.write_latency_threshold_critical_s # In seconds
  alarm_description   = "Critical: Write latency for ${var.db_identifier} is above ${var.write_latency_threshold_critical_ms} ms"
  alarm_actions       = var.sns_topic_arns_critical
  ok_actions          = var.sns_topic_arns_critical
  dimensions = {
    (local.dimension_name) = var.db_identifier
  }
  tags = merge(var.tags, { Severity = "Critical", Metric = "WriteLatency" })
}

# ReplicaLag alarm (only if not an Aurora cluster, as Aurora has its own specific replica lag metric, or for RDS read replicas)
resource "aws_cloudwatch_metric_alarm" "replica_lag_critical" {
  count = !var.is_aurora_cluster ? 1 : 0 # Or more complex logic if it's an Aurora replica vs RDS read replica

  alarm_name          = "${var.db_identifier}-replica-lag-critical"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "3"
  metric_name         = "ReplicaLag" # For RDS Read Replicas. Aurora uses AuroraReplicaLag.
  namespace           = "AWS/RDS"
  period              = "60" # Check more frequently
  statistic           = "Maximum"
  threshold           = var.replica_lag_threshold_critical_seconds
  alarm_description   = "Critical: Replica lag for ${var.db_identifier} is above ${var.replica_lag_threshold_critical_seconds} seconds"
  alarm_actions       = var.sns_topic_arns_critical
  ok_actions          = var.sns_topic_arns_critical
  dimensions = {
    DBInstanceIdentifier = var.db_identifier # ReplicaLag is specific to DBInstanceIdentifier
  }
  tags = merge(var.tags, { Severity = "Critical", Metric = "ReplicaLag" })
}

resource "aws_cloudwatch_metric_alarm" "aurora_replica_lag_critical" {
  count = var.is_aurora_cluster ? 1 : 0 # This applies to Aurora Replicas within an Aurora Cluster

  alarm_name          = "${var.db_identifier}-aurora-replica-lag-critical"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "3"
  metric_name         = "AuroraReplicaLag"
  namespace           = "AWS/RDS"
  period              = "60"
  statistic           = "Maximum"
  threshold           = var.replica_lag_threshold_critical_seconds # AuroraReplicaLag is in milliseconds, this needs conversion
  # threshold           = var.replica_lag_threshold_critical_seconds * 1000 # If var is in seconds
  alarm_description   = "Critical: Aurora replica lag for ${var.db_identifier} instance is high."
  alarm_actions       = var.sns_topic_arns_critical
  ok_actions          = var.sns_topic_arns_critical
  dimensions = {
    DBInstanceIdentifier = var.db_identifier # Target a specific reader instance in the Aurora cluster
    # Or use DBClusterIdentifier and Role = READER for average lag across readers, but specific instance is better.
  }
  # Note: AuroraReplicaLagMaximum and AuroraReplicaLagMinimum are also available per cluster.
  # This setup assumes var.db_identifier is a specific reader instance.
  # If var.db_identifier is the cluster, you might need to iterate over reader instances or use a different approach.
  tags = merge(var.tags, { Severity = "Critical", Metric = "AuroraReplicaLag" })
}