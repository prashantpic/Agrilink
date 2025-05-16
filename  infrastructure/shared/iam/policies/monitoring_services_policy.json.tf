# Purpose: Grant necessary permissions for monitoring agents/services
# (like CloudWatch agent, Prometheus EKS service accounts) to collect metrics and logs.
# LogicDescription: Defines permissions using 'aws_iam_policy_document' for actions
# such as 'cloudwatch:PutMetricData', 'logs:CreateLogStream', 'logs:PutLogEvents',
# 'ec2:DescribeInstances' (for discovery), and other permissions required by
# specific monitoring tools running on EC2 or EKS.
# ImplementedFeatures: CloudWatch Metrics/Logs Publishing Policy, EC2/EKS Discovery for Monitoring
# RequirementIds: REQ-17-005

# This data source defines an IAM policy document for monitoring services.
# It may require variables for specific resource targeting if not using wildcards.
# For REQ-17-005, this policy will be used by agents/services.

data "aws_iam_policy_document" "monitoring_services_policy" {
  # Permissions for CloudWatch Agent / general metric and log publishing
  statement {
    sid    = "CloudWatchMetricsLogsPublish"
    effect = "Allow"
    actions = [
      "cloudwatch:PutMetricData",
      "logs:CreateLogGroup", # If agent needs to create log groups
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:DescribeLogStreams"
    ]
    resources = ["*"] # CloudWatch actions are typically region-wide, not resource-specific for creation/put
  }

  # Permissions for EC2 instance discovery (e.g., by CloudWatch agent or Prometheus node_exporter sidecar)
  statement {
    sid    = "EC2Discovery"
    effect = "Allow"
    actions = [
      "ec2:DescribeInstances",
      "ec2:DescribeTags" # Often needed to filter instances
    ]
    resources = ["*"] # Describe actions are global or regional, not resource-specific
  }
  
  # Permissions for AutoScaling group discovery (if monitoring relies on ASG info)
  statement {
    sid    = "AutoScalingDiscovery"
    effect = "Allow"
    actions = [
      "autoscaling:DescribeAutoScalingGroups",
      "autoscaling:DescribeAutoScalingInstances"
    ]
    resources = ["*"]
  }

  # Permissions for EKS discovery (e.g., Prometheus operator scraping EKS components)
  # These are typically Kubernetes RBAC permissions, but if an AWS SDK is used for EKS discovery:
  statement {
    sid    = "EKSDiscovery"
    effect = "Allow"
    actions = [
      "eks:DescribeCluster" # Allows getting cluster details like OIDC provider URL
      # "eks:ListNodegroups",
      # "eks:DescribeNodegroup"
    ]
    # Typically on specific cluster ARN or "*" if role is very specific
    resources = var.eks_cluster_arns_for_monitoring # Variable: list of EKS cluster ARNs
  }

  # Permissions for AWS X-Ray (if used for APM traces)
  # statement {
  #   sid    = "XRayAccess"
  #   effect = "Allow"
  #   actions = [
  #     "xray:PutTraceSegments",
  #     "xray:PutTelemetryRecords",
  #     "xray:GetSamplingRules",
  #     "xray:GetSamplingTargets",
  #     "xray:GetSamplingStatisticSummaries"
  #   ]
  #   resources = ["*"]
  # }

  # Permissions for Prometheus Remote Write to AWS Managed Prometheus (AMP)
  # statement {
  #   sid    = "PrometheusRemoteWriteToAMP"
  #   effect = "Allow"
  #   actions = [
  #     "aps:RemoteWrite",
  #     "aps:GetSeries",
  #     "aps:GetLabels",
  #     "aps:GetMetricMetadata"
  #   ]
  #   resources = [var.amp_workspace_arn] # Variable: ARN of the AMP workspace
  # }
}

# To use this:
#
# variable "eks_cluster_arns_for_monitoring" {
#   description = "List of EKS cluster ARNs that monitoring services can describe."
#   type        = list(string)
#   default     = ["*"] # Or provide specific ARNs
# }
#
# data "aws_iam_policy_document" "monitoring_services_policy" {
#   # ... content from above ...
# }
#
# resource "aws_iam_policy" "my_monitoring_policy" {
#   name   = "my-monitoring-services-policy"
#   policy = data.aws_iam_policy_document.monitoring_services_policy.json
# }