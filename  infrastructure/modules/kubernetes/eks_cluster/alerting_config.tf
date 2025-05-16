# Description: Configures cluster-level alerting components like kube-state-metrics and node-exporter.
# Purpose: Set up alerts for Kubernetes cluster health, resource utilization, and core component status.
# Requirements: REQ-17-006, REQ-17-007

variable "eks_cluster_name" {
  description = "Name of the EKS cluster."
  type        = string
}

variable "tags" {
  description = "A map of tags to assign to the resources."
  type        = map(string)
  default     = {}
}

variable "deploy_kube_state_metrics" {
  description = "Flag to deploy kube-state-metrics. Set to false if deployed by another stack (e.g., kube-prometheus-stack)."
  type        = bool
  default     = true
}

variable "kube_state_metrics_namespace" {
  description = "Namespace for kube-state-metrics."
  type        = string
  default     = "kube-system" # Often deployed in kube-system or a dedicated monitoring namespace
}

variable "kube_state_metrics_helm_chart_version" {
  description = "Version of the kube-state-metrics Helm chart."
  type        = string
  default     = "5.15.2" # Check for latest stable community chart version
}

variable "deploy_node_exporter" {
  description = "Flag to deploy node-exporter. Set to false if deployed by another stack (e.g., kube-prometheus-stack)."
  type        = bool
  default     = true
}

variable "node_exporter_namespace" {
  description = "Namespace for node-exporter."
  type        = string
  default     = "kube-system" # Often deployed in kube-system or a dedicated monitoring namespace
}

variable "node_exporter_helm_chart_version" {
  description = "Version of the node-exporter Helm chart."
  type        = string
  default     = "4.23.0" # Check for latest stable community chart version
}

# Note: The kube-prometheus-stack (deployed via modules/monitoring/prometheus_stack) typically includes
# kube-state-metrics and node-exporter. This file provides them as standalone options
# if a different Prometheus setup is used or if they need to be managed separately.
# Alert rules themselves are primarily defined in the prometheus_stack module. This file focuses on metric exporters.

# Kube-State-Metrics Deployment
resource "helm_release" "kube_state_metrics" {
  count      = var.deploy_kube_state_metrics ? 1 : 0
  name       = "kube-state-metrics"
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "kube-state-metrics"
  namespace  = var.kube_state_metrics_namespace
  version    = var.kube_state_metrics_helm_chart_version

  # Default values are usually sufficient for basic operation.
  # Custom values can be added here if needed, e.g., resource requests/limits, node selectors.
  # values = [
  #   yamlencode({
  #     rbac = {
  #       create = true
  #     }
  #     serviceAccount = {
  #       create = true
  #     }
  #     # Ensure Prometheus Operator ServiceMonitor is created if that's your setup
  #     # prometheus = {
  #     #   monitor = {
  #     #     enabled = true 
  #     #     # namespace = "monitoring" # Namespace where Prometheus Operator is running
  #     #   }
  #     # }
  #   })
  # ]

  # Ensure namespace exists if not kube-system (which always exists)
  # depends_on = [kubernetes_namespace.monitoring] # Example if using a custom monitoring namespace
}

# Node-Exporter Deployment
resource "helm_release" "node_exporter" {
  count      = var.deploy_node_exporter ? 1 : 0
  name       = "node-exporter"
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "prometheus-node-exporter" # Chart name in the prometheus-community repo
  namespace  = var.node_exporter_namespace
  version    = var.node_exporter_helm_chart_version

  # Default values are usually sufficient. Node exporter runs as a DaemonSet.
  # Custom values can be added here if needed.
  # values = [
  #   yamlencode({
  #     rbac = {
  #       create = true
  #     }
  #     serviceAccount = {
  #       create = true
  #     }
  #     # Ensure Prometheus Operator ServiceMonitor is created if that's your setup
  #     # prometheus = {
  #     #   monitor = {
  #     #     enabled = true
  #     #     # namespace = "monitoring" # Namespace where Prometheus Operator is running
  #     #   }
  #     # }
  #   })
  # ]
  # depends_on = [kubernetes_namespace.monitoring] # Example
}


# RBAC for kube-state-metrics and node-exporter are typically handled by their respective Helm charts.
# If deployed as part of a larger stack like kube-prometheus-stack, that chart manages their RBAC.
# This file primarily ensures these components are deployed if not covered elsewhere.
# The actual alert rules (e.g., NodeNotReady, PodCrashLooping) would be defined
# in `modules/monitoring/prometheus_stack/alert_rules.tf` as PrometheusRule CRs.

output "kube_state_metrics_deployed" {
  description = "Indicates if kube-state-metrics was deployed by this module."
  value       = var.deploy_kube_state_metrics
}

output "node_exporter_deployed" {
  description = "Indicates if node-exporter was deployed by this module."
  value       = var.deploy_node_exporter
}