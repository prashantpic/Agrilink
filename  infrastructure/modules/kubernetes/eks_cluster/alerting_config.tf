# Purpose: Set up alerts for Kubernetes cluster health, resource utilization, and core component status.
# LogicDescription: Deploys essential Kubernetes metrics exporters like 'kube-state-metrics' and 'node-exporter'.
# These are typically scraped by a Prometheus instance (often deployed as part of a separate Prometheus stack).
# REQ-17-006: Alerting Logic for cluster health
# REQ-17-007: Alert Conditions for cluster health

variable "deploy_kube_state_metrics" {
  description = "Flag to deploy kube-state-metrics. Set to false if managed by another stack (e.g., kube-prometheus-stack)."
  type        = bool
  default     = true
}

variable "kube_state_metrics_namespace" {
  description = "Namespace for kube-state-metrics."
  type        = string
  default     = "kube-system"
}

variable "kube_state_metrics_helm_chart_version" {
  description = "Version of the kube-state-metrics Helm chart."
  type        = string
  default     = "5.19.0" # Check https://github.com/kubernetes/kube-state-metrics/tree/main/charts/kube-state-metrics for latest
}

variable "deploy_node_exporter" {
  description = "Flag to deploy node-exporter. Set to false if managed by another stack (e.g., kube-prometheus-stack)."
  type        = bool
  default     = true
}

variable "node_exporter_namespace" {
  description = "Namespace for node-exporter."
  type        = string
  default     = "kube-system"
}

variable "node_exporter_helm_chart_version" {
  description = "Version of the prometheus-node-exporter Helm chart."
  type        = string
  default     = "4.30.0" # Check https://github.com/prometheus-community/helm-charts for prometheus-node-exporter chart version
}

variable "tags" {
  description = "A map of tags to add to all resources."
  type        = map(string)
  default     = {}
}

# Kube State Metrics Deployment (via Helm)
resource "helm_release" "kube_state_metrics" {
  count      = var.deploy_kube_state_metrics ? 1 : 0
  name       = "kube-state-metrics"
  repository = "https://kubernetes.github.io/kube-state-metrics"
  chart      = "kube-state-metrics"
  namespace  = var.kube_state_metrics_namespace
  version    = var.kube_state_metrics_helm_chart_version

  # Default values are generally fine for kube-state-metrics.
  # Adjust resources, replicaCount, etc. as needed.
  # Example:
  # values = [
  #   yamlencode({
  #     replicas = 1
  #     resources = {
  #       requests = {
  #         cpu    = "100m"
  #         memory = "128Mi"
  #       }
  #       limits = {
  #         cpu    = "200m"
  #         memory = "256Mi"
  #       }
  #     }
  #   })
  # ]
}

# Node Exporter Deployment (via Helm)
# This deploys prometheus-community/prometheus-node-exporter
resource "helm_release" "node_exporter" {
  count      = var.deploy_node_exporter ? 1 : 0
  name       = "node-exporter"
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "prometheus-node-exporter"
  namespace  = var.node_exporter_namespace
  version    = var.node_exporter_helm_chart_version

  # Default values are generally fine for node-exporter.
  # It runs as a DaemonSet.
  # Ensure it has necessary host permissions (hostNetwork, hostPID, hostPath for /proc, /sys, /host/rootfs)
  # The chart typically handles these.
  # Example:
  # values = [
  #   yamlencode({
  #     resources = {
  #       requests = {
  #         cpu    = "50m"
  #         memory = "64Mi"
  #       }
  #       limits = {
  #         cpu    = "100m"
  #         memory = "128Mi"
  #       }
  #     }
  #     # If you need to enable specific collectors:
  #     # extraArgs = [
  #     #   "--collector.filesystem.ignored-mount-points=^/(dev|proc|sys|var/lib/docker/.+|var/lib/kubelet/pods/.+)($|/)"
  #     # ]
  #   })
  # ]
}

# Note: Foundational alert rules for cluster components (e.g., NodeNotReady, PodCrashLooping)
# are typically defined as PrometheusRule CRs. These are better placed in the
# `modules/monitoring/prometheus_stack/alert_rules.tf` file, assuming Prometheus Operator
# is used from that stack. This file focuses on deploying the exporters.
# If Prometheus is not used, or a different alerting mechanism is in place,
# then alert definitions (e.g., CloudWatch Alarms based on EKS control plane logs/metrics)
# might be considered here or in a dedicated CloudWatch alarms module.

output "kube_state_metrics_deployed" {
  description = "Indicates if kube-state-metrics was deployed by this module."
  value       = var.deploy_kube_state_metrics
}

output "node_exporter_deployed" {
  description = "Indicates if node-exporter was deployed by this module."
  value       = var.deploy_node_exporter
}