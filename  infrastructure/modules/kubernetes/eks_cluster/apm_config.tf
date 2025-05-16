# Purpose: Enable Application Performance Monitoring for applications running on EKS.
# LogicDescription: Deploys and configures APM agents/collectors.
# This example focuses on setting up RBAC and IRSA for an OpenTelemetry Collector.
# Actual deployment of OpenTelemetry Collector can be done via Helm or Kubernetes manifests.
# REQ-17-005: APM Metrics Collection

variable "eks_cluster_name" {
  description = "The name of the EKS cluster."
  type        = string
}

variable "eks_oidc_provider_arn" {
  description = "ARN of the OIDC provider for the EKS cluster."
  type        = string
}

variable "aws_region" {
  description = "AWS region for resource deployment."
  type        = string
}

variable "deploy_otel_collector" {
  description = "Flag to indicate if OpenTelemetry Collector components should be configured by this module."
  type        = bool
  default     = false # Assume a central Prometheus/APM stack might handle this
}

variable "otel_collector_namespace" {
  description = "Kubernetes namespace for OpenTelemetry Collector."
  type        = string
  default     = "opentelemetry-collector"
}

variable "otel_collector_service_account_name" {
  description = "Service account name for OpenTelemetry Collector."
  type        = string
  default     = "otel-collector-sa"
}

variable "otel_collector_helm_chart_version" {
  description = "Version of the OpenTelemetry Collector Helm chart."
  type        = string
  default     = "0.92.0" # Check for latest stable version of opentelemetry-collector chart
}

variable "otel_aws_xray_enabled" {
  description = "Enable AWS X-Ray exporter for OpenTelemetry Collector."
  type        = bool
  default     = false
}

variable "otel_cloudwatch_emf_enabled" {
  description = "Enable CloudWatch EMF exporter for OpenTelemetry Collector."
  type        = bool
  default     = false
}

variable "tags" {
  description = "A map of tags to add to all resources."
  type        = map(string)
  default     = {}
}

data "aws_iam_policy_document" "otel_collector_assume_role_policy" {
  count = var.deploy_otel_collector && (var.otel_aws_xray_enabled || var.otel_cloudwatch_emf_enabled) ? 1 : 0
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"
    principals {
      type        = "Federated"
      identifiers = [var.eks_oidc_provider_arn]
    }
    condition {
      test     = "StringEquals"
      variable = "${replace(var.eks_oidc_provider_arn, "oidc-provider/", "oidc.eks.${var.aws_region}.amazonaws.com/id/")}:sub"
      values   = ["system:serviceaccount:${var.otel_collector_namespace}:${var.otel_collector_service_account_name}"]
    }
    condition {
      test     = "StringEquals"
      variable = "${replace(var.eks_oidc_provider_arn, "oidc-provider/", "oidc.eks.${var.aws_region}.amazonaws.com/id/")}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "otel_collector_role" {
  count              = var.deploy_otel_collector && (var.otel_aws_xray_enabled || var.otel_cloudwatch_emf_enabled) ? 1 : 0
  name               = "${var.eks_cluster_name}-otel-collector-role"
  assume_role_policy = data.aws_iam_policy_document.otel_collector_assume_role_policy[0].json
  tags               = merge(var.tags, { Name = "${var.eks_cluster_name}-otel-collector-role" })
}

resource "aws_iam_policy_attachment" "otel_xray_policy_attach" {
  count      = var.deploy_otel_collector && var.otel_aws_xray_enabled ? 1 : 0
  role       = aws_iam_role.otel_collector_role[0].name
  policy_arn = "arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess"
}

resource "aws_iam_policy_attachment" "otel_cloudwatch_agent_policy_attach" {
  count      = var.deploy_otel_collector && var.otel_cloudwatch_emf_enabled ? 1 : 0
  role       = aws_iam_role.otel_collector_role[0].name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy" # Grants permissions to write logs and metrics
}

resource "kubernetes_namespace_v1" "otel_collector_ns" {
  count = var.deploy_otel_collector ? 1 : 0
  metadata {
    name   = var.otel_collector_namespace
    labels = { name = var.otel_collector_namespace }
  }
}

resource "kubernetes_service_account_v1" "otel_collector_sa" {
  count = var.deploy_otel_collector ? 1 : 0
  metadata {
    name      = var.otel_collector_service_account_name
    namespace = kubernetes_namespace_v1.otel_collector_ns[0].metadata[0].name
    annotations = merge(
      (var.otel_aws_xray_enabled || var.otel_cloudwatch_emf_enabled) ? { "eks.amazonaws.com/role-arn" = aws_iam_role.otel_collector_role[0].arn } : {}
    )
  }
  depends_on = [kubernetes_namespace_v1.otel_collector_ns]
}

# RBAC for OpenTelemetry Collector to scrape Kubernetes metrics/metadata
resource "kubernetes_cluster_role_v1" "otel_collector_cluster_role" {
  count = var.deploy_otel_collector ? 1 : 0
  metadata {
    name = "opentelemetry-collector-metrics"
  }
  rule {
    api_groups = [""]
    resources  = ["nodes", "nodes/proxy", "nodes/metrics", "services", "endpoints", "pods", "namespaces", "events"]
    verbs      = ["get", "list", "watch"]
  }
  rule {
    api_groups = ["extensions", "apps"]
    resources  = ["daemonsets", "deployments", "replicasets", "statefulsets"]
    verbs      = ["get", "list", "watch"]
  }
  rule {
    api_groups = ["autoscaling"]
    resources  = ["horizontalpodautoscalers"]
    verbs      = ["get", "list", "watch"]
  }
  rule {
    non_resource_urls = ["/metrics", "/metrics/cadvisor"]
    verbs             = ["get"]
  }
}

resource "kubernetes_cluster_role_binding_v1" "otel_collector_cluster_role_binding" {
  count = var.deploy_otel_collector ? 1 : 0
  metadata {
    name = "opentelemetry-collector-metrics-binding"
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "ClusterRole"
    name      = kubernetes_cluster_role_v1.otel_collector_cluster_role[0].metadata[0].name
  }
  subject {
    kind      = "ServiceAccount"
    name      = kubernetes_service_account_v1.otel_collector_sa[0].metadata[0].name
    namespace = kubernetes_namespace_v1.otel_collector_ns[0].metadata[0].name
  }
  depends_on = [
    kubernetes_cluster_role_v1.otel_collector_cluster_role,
    kubernetes_service_account_v1.otel_collector_sa
  ]
}

# Example: Deploying OpenTelemetry Collector using Helm
# This is a basic example; a real-world deployment would need a more detailed values file.
resource "helm_release" "opentelemetry_collector" {
  count      = var.deploy_otel_collector ? 1 : 0
  name       = "otel-collector"
  repository = "https://open-telemetry.github.io/opentelemetry-helm-charts"
  chart      = "opentelemetry-collector"
  namespace  = kubernetes_namespace_v1.otel_collector_ns[0].metadata[0].name
  version    = var.otel_collector_helm_chart_version

  values = [
    <<-EOF
mode: "daemonset" # or "deployment" or "statefulset"
serviceAccount:
  create: false # We created it above for IRSA
  name: ${var.otel_collector_service_account_name}
presets:
  # Enable kubernetes attributes processor
  kubernetesAttributes:
    enabled: true
  # Enable k8s cluster receiver for cluster-level metrics
  kubeletMetrics:
    enabled: true
  # Enable host metrics
  hostMetrics:
    enabled: true

config:
  exporters:
    logging:
      loglevel: debug
    # prometheus: # Example if you want OTEL to expose a Prometheus endpoint
    #   endpoint: "0.0.0.0:8889"
    # awsxray: ${var.otel_aws_xray_enabled ? "{}" : "null"} # Configure if enabled
    # awsemf: ${var.otel_cloudwatch_emf_enabled ? "{ region: \"${var.aws_region}\" }" : "null"} # Configure if enabled
  service:
    pipelines:
      metrics:
        receivers: [otlp, prometheus] # Example receivers
        processors: [memory_limiter, batch]
        exporters: [logging] # Add awsxray, awsemf, prometheus as needed
      traces:
        receivers: [otlp, zipkin, jaeger] # Example receivers
        processors: [memory_limiter, batch]
        exporters: [logging] # Add awsxray as needed
      logs:
        receivers: [otlp, fluentforward] # Example receivers
        processors: [memory_limiter, batch]
        exporters: [logging] # Add awsemf as needed
  # Add more OTEL collector configuration as needed
EOF
  ]

  depends_on = [
    kubernetes_service_account_v1.otel_collector_sa,
    kubernetes_cluster_role_binding_v1.otel_collector_cluster_role_binding,
    aws_iam_role.otel_collector_role # Ensure role exists if used
  ]
}

output "otel_collector_irsa_role_arn" {
  description = "ARN of the IAM role for OpenTelemetry Collector if created."
  value       = var.deploy_otel_collector && (var.otel_aws_xray_enabled || var.otel_cloudwatch_emf_enabled) ? aws_iam_role.otel_collector_role[0].arn : null
}

output "otel_collector_namespace" {
  description = "Kubernetes namespace for OpenTelemetry Collector if deployed."
  value       = var.deploy_otel_collector ? var.otel_collector_namespace : null
}