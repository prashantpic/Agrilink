# Purpose: Grant necessary permissions for monitoring agents/services to collect metrics and logs.
# LogicDescription: Defines permissions using 'aws_iam_policy_document' for actions such as
# 'cloudwatch:PutMetricData', 'logs:CreateLogStream', 'logs:PutLogEvents', 'ec2:DescribeInstances', etc.
# Requirement: REQ-17-005

# This policy is a general template. Specific permissions might need adjustment based on
# the exact monitoring tools and their requirements (e.g., Prometheus Node Exporter, CloudWatch Agent, OpenTelemetry Collector).

variable "allow_cloudwatch_metrics_publish" {
  description = "Allow publishing metrics to CloudWatch."
  type        = bool
  default     = true
}

variable "allow_cloudwatch_logs_publish" {
  description = "Allow publishing logs to CloudWatch Logs."
  type        = bool
  default     = true
}

variable "allow_ec2_describe" {
  description = "Allow describing EC2 instances (for discovery)."
  type        = bool
  default     = true
}

variable "allow_eks_describe" {
  description = "Allow describing EKS cluster (for discovery by EKS monitoring tools)."
  type        = bool
  default     = false # Enable if specific EKS monitoring tools need it for IRSA
}

variable "eks_cluster_arn_for_describe" {
  description = "Specific EKS cluster ARN if allow_eks_describe is true."
  type        = string
  default     = null
}

variable "additional_iam_statements" {
  description = "A list of additional IAM statement objects to include in the policy."
  type        = list(any) # list of objects matching IAM statement structure
  default     = []
}

data "aws_iam_policy_document" "monitoring_services_policy" {
  dynamic "statement" {
    for_each = var.allow_cloudwatch_metrics_publish ? [1] : []
    content {
      sid    = "AllowCloudWatchPutMetricData"
      effect = "Allow"
      actions = [
        "cloudwatch:PutMetricData"
      ]
      resources = ["*"] # PutMetricData does not support resource-level permissions for custom metrics
    }
  }

  dynamic "statement" {
    for_each = var.allow_cloudwatch_logs_publish ? [1] : []
    content {
      sid    = "AllowCloudWatchLogs"
      effect = "Allow"
      actions = [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogStreams"
      ]
      resources = ["arn:aws:logs:*:*:*"] # Or restrict to specific log group prefixes
    }
  }

  dynamic "statement" {
    for_each = var.allow_ec2_describe ? [1] : []
    content {
      sid    = "AllowEC2Describe"
      effect = "Allow"
      actions = [
        "ec2:DescribeInstances",
        "ec2:DescribeTags", # Often needed with DescribeInstances for filtering/enrichment
        "autoscaling:DescribeAutoScalingGroups" # For ASG discovery
      ]
      resources = ["*"] # Describe actions are typically global or region-wide
    }
  }

  dynamic "statement" {
    for_each = var.allow_eks_describe && var.eks_cluster_arn_for_describe != null ? [1] : []
    content {
      sid    = "AllowEKSDescribe"
      effect = "Allow"
      actions = [
        "eks:DescribeCluster"
        # Add other EKS permissions if needed, e.g., eks:ListNodegroups
      ]
      resources = [var.eks_cluster_arn_for_describe]
    }
  }

  dynamic "statement" {
    for_each = var.additional_iam_statements
    content {
      sid       = lookup(statement.value, "sid", null)
      effect    = lookup(statement.value, "effect", "Allow")
      actions   = lookup(statement.value, "actions", [])
      resources = lookup(statement.value, "resources", [])
      dynamic "condition" {
        for_each = lookup(statement.value, "condition", [])
        content {
          test     = condition.value.test
          variable = condition.value.variable
          values   = condition.value.values
        }
      }
    }
  }
}

output "policy_json" {
  description = "The IAM policy document JSON for monitoring services."
  value       = data.aws_iam_policy_document.monitoring_services_policy.json
}