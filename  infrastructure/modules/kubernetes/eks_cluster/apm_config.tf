# Description: Configures APM tool deployment (e.g., OpenTelemetry agent) within the EKS cluster.
# Purpose: Enable Application Performance Monitoring for applications running on EKS.
# Requirements: REQ-17-005

variable "eks_cluster_name" {
  description = "Name of the EKS cluster."
  type        = string
}

variable "eks_oidc_provider_arn" {
  description = "ARN of the EKS OIDC provider."
  type        = string
}

variable "eks_oidc_provider_url" {
  description = "URL of the EKS OIDC provider (without https://)."
  type        = string
}

variable "aws_region" {
  description = "AWS region for the resources."
  type        = string
}

variable "tags" {
  description = "A map of tags to assign to the resources."
  type        = map(string)
  default     = {}
}

variable "deploy_opentelemetry_collector" {
  description = "Flag to deploy OpenTelemetry Collector."
  type        = bool
  default     = false # Set to true to deploy
}

variable "opentelemetry_collector_namespace" {
  description = "Namespace to deploy OpenTelemetry Collector."
  type        = string
  default     = "opentelemetry-system"
}

variable "opentelemetry_collector_helm_chart_version" {
  description = "Version of the OpenTelemetry Collector Helm chart."
  type        = string
  default     = "0.70.0" # Check for latest stable version of 'opentelemetry-collector'
}

variable "opentelemetry_collector_iam_role_name" {
  description = "Name for the IAM role for OpenTelemetry Collector service account. If empty, a default will be generated."
  type        = string
  default     = ""
}

variable "opentelemetry_aws_monitoring_policy_arn" {
  description = "ARN of an existing IAM policy granting permissions for OpenTelemetry to write to AWS monitoring services (e.g., CloudWatch, X-Ray). If empty, a minimal policy will be created."
  type        = string
  default     = "" # Example: "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy", "arn:aws:iam::aws:policy/AWSXrayWriteOnlyAccess" or custom policy
}

# This file primarily focuses on deploying APM agents like OpenTelemetry Collector.
# Prometheus operator, kube-prometheus-stack are expected to be deployed via the dedicated `prometheus_stack` module.
# If Prometheus is the primary APM, this file might configure specific Prometheus scrape jobs or service monitors
# if they are not auto-discovered or need EKS-specific setup.

resource "kubernetes_namespace" "opentelemetry_collector" {
  count = var.deploy_opentelemetry_collector ? 1 : 0
  metadata {
    name = var.opentelemetry_collector_namespace
    labels = {
      name = var.opentelemetry_collector_namespace
    }
  }
}

# IAM Role for OpenTelemetry Collector Service Account (IRSA) - if sending data to AWS services like X-Ray, CloudWatch EMF
data "aws_iam_policy_document" "opentelemetry_collector_assume_role_policy" {
  count = var.deploy_opentelemetry_collector ? 1 : 0
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"
    principals {
      type        = "Federated"
      identifiers = [var.eks_oidc_provider_arn]
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider_url}:sub"
      # This needs to match the service account name used by the OpenTelemetry Collector Helm chart
      values = ["system:serviceaccount:${var.opentelemetry_collector_namespace}:otel-collector-opentelemetry-collector"]
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider_url}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "opentelemetry_collector" {
  count              = var.deploy_opentelemetry_collector ? 1 : 0
  name_prefix        = var.opentelemetry_collector_iam_role_name == "" ? "OTel-${var.eks_cluster_name}-" : var.opentelemetry_collector_iam_role_name
  assume_role_policy = data.aws_iam_policy_document.opentelemetry_collector_assume_role_policy[0].json
  tags               = merge(var.tags, { Name = var.opentelemetry_collector_iam_role_name == "" ? "OTel-Role-${var.eks_cluster_name}" : var.opentelemetry_collector_iam_role_name })
}

data "aws_iam_policy_document" "opentelemetry_collector_aws_access" {
  count = var.deploy_opentelemetry_collector && var.opentelemetry_aws_monitoring_policy_arn == "" ? 1 : 0
  # Minimal permissions for AWS Distro for OpenTelemetry (ADOT) to send to X-Ray and CloudWatch
  statement {
    sid    = "AWSOTelDefaultMetricsAndTraces"
    effect = "Allow"
    actions = [
      "xray:PutTraceSegments",
      "xray:PutTelemetryRecords",
      "xray:GetSamplingRules",
      "xray:GetSamplingTargets",
      "cloudwatch:PutMetricData",
      "logs:PutLogEvents",
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:DescribeLogStreams",
      "logs:DescribeLogGroups",
      "ssm:GetParameters" # For ADOT config discovery
    ]
    resources = ["*"] # Consider scoping down resources if possible
  }
}

resource "aws_iam_policy" "opentelemetry_collector_aws_policy" {
  count       = var.deploy_opentelemetry_collector && var.opentelemetry_aws_monitoring_policy_arn == "" ? 1 : 0
  name_prefix = "OTel-Policy-${var.eks_cluster_name}-"
  policy      = data.aws_iam_policy_document.opentelemetry_collector_aws_access[0].json
  tags        = merge(var.tags, { Name = "OTel-Policy-${var.eks_cluster_name}" })
}

resource "aws_iam_role_policy_attachment" "opentelemetry_collector_attach" {
  count      = var.deploy_opentelemetry_collector ? 1 : 0
  role       = aws_iam_role.opentelemetry_collector[0].name
  policy_arn = var.opentelemetry_aws_monitoring_policy_arn != "" ? var.opentelemetry_aws_monitoring_policy_arn : aws_iam_policy.opentelemetry_collector_aws_policy[0].arn
}

# Deploy OpenTelemetry Collector using Helm (Example for ADOT Collector)
# Chart: https://github.com/aws-observability/aws-otel-helm-charts
resource "helm_release" "opentelemetry_collector" {
  count      = var.deploy_opentelemetry_collector ? 1 : 0
  name       = "adot-collector" # AWS Distro for OpenTelemetry
  repository = "https://aws-observability.github.io/aws-otel-helm-charts"
  chart      = "adot" # formerly "aws-otel-collector" or "opentelemetry-collector" from community
  namespace  = kubernetes_namespace.opentelemetry_collector[0].metadata[0].name
  version    = var.opentelemetry_collector_helm_chart_version # Ensure this matches the ADOT chart version

  values = [
    yamlencode({
      # fullnameOverride = "otel-collector" # Ensure this matches SA name in assume role policy if overridden
      clusterName = var.eks_cluster_name
      awsRegion   = var.aws_region
      serviceAccount = {
        create = true
        name   = "otel-collector-opentelemetry-collector" # default SA name for the community chart, ADOT might differ. Verify!
        annotations = {
          "eks.amazonaws.com/role-arn" = aws_iam_role.opentelemetry_collector[0].arn
        }
      }
      # Example: Enable AWS X-Ray exporter
      # config = {
      #   exporters = {
      #     awsxray = {}
      #   }
      #   service = {
      #     pipelines = {
      #       traces = {
      #         receivers = ["otlp"]
      #         exporters = ["awsxray"]
      #       }
      #     }
      #   }
      # }
      # Example: Enable metrics collection and export to CloudWatch EMF
      # daemonSet = { # For node-level metrics
      #   enabled = true
      # }
      # deployment = { # For cluster-level metrics/traces
      #   enabled = true
      # }
      # See ADOT chart documentation for detailed configuration options:
      # https://aws-observability.github.io/aws-otel-helm-charts/charts/adot/
    })
  ]

  depends_on = [
    kubernetes_namespace.opentelemetry_collector[0],
    aws_iam_role_policy_attachment.opentelemetry_collector_attach[0],
  ]
}

output "opentelemetry_collector_namespace_name" {
  description = "Namespace where OpenTelemetry Collector is deployed."
  value       = var.deploy_opentelemetry_collector ? kubernetes_namespace.opentelemetry_collector[0].metadata[0].name : null
}

output "opentelemetry_collector_iam_role_arn" {
  description = "ARN of the IAM role for the OpenTelemetry Collector, if deployed."
  value       = var.deploy_opentelemetry_collector ? aws_iam_role.opentelemetry_collector[0].arn : null
}