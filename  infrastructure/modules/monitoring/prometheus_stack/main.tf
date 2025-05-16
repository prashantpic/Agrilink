# Purpose: Set up a comprehensive monitoring and alerting solution using the Prometheus stack on Kubernetes.
# LogicDescription: Deploys Prometheus for metrics collection and storage, Alertmanager for handling alerts,
# and Grafana for visualization. This is typically achieved by deploying the kube-prometheus-stack Helm chart.
# Configures persistent storage for Prometheus and Grafana, service discovery mechanisms, and basic Alertmanager routing.
# Requirements: REQ-17-005, REQ-17-006

terraform {
  required_providers {
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.5"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.10"
    }
  }
}

# Assumes Kubernetes and Helm providers are configured by the caller (e.g., environment configuration)
# that has access to the EKS cluster details.

# Example provider configuration (would be in the calling module/environment):
# provider "kubernetes" {
#   host                   = module.eks_cluster.cluster_endpoint
#   cluster_ca_certificate = base64decode(module.eks_cluster.cluster_certificate_authority_data)
#   token                  = data.aws_eks_cluster_auth.main.token
# }
# provider "helm" {
#   kubernetes {
#     host                   = module.eks_cluster.cluster_endpoint
#     cluster_ca_certificate = base64decode(module.eks_cluster.cluster_certificate_authority_data)
#     token                  = data.aws_eks_cluster_auth.main.token
#   }
# }

resource "kubernetes_namespace" "monitoring" {
  count = var.create_namespace ? 1 : 0
  metadata {
    name = var.namespace
    labels = merge(
      var.common_labels,
      {
        "app.kubernetes.io/managed-by" = "Terraform"
      }
    )
  }
}

resource "helm_release" "kube_prometheus_stack" {
  name       = var.release_name
  repository = var.helm_repository_url
  chart      = var.helm_chart_name
  version    = var.helm_chart_version
  namespace  = var.namespace

  values = [yamlencode(var.helm_values)]

  # Set dynamic values if needed, e.g.
  # set {
  #   name  = "prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues"
  #   value = "false" # Example, refer to chart documentation
  # }
  # set {
  #   name = "alertmanager.config.global.sns_api_url" # Example if using SNS receiver for Alertmanager
  #   value = var.alertmanager_sns_api_url # e.g., http://localhost:9095 for alertmanager-sns-forwarder
  # }
  # set {
  #   name = "grafana.persistence.enabled"
  #   value = var.grafana_persistence_enabled
  # }
  # set {
  #   name = "grafana.persistence.storageClassName"
  #   value = var.grafana_storage_class_name # e.g. "gp2" or custom SC
  # }
  # set {
  #   name = "prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.storageClassName"
  #   value = var.prometheus_storage_class_name # e.g. "gp2" or custom SC
  # }

  dynamic "set" {
    for_each = var.helm_set_values
    content {
      name  = set.key
      value = set.value
    }
  }

  dynamic "set_sensitive" {
    for_each = var.helm_set_sensitive_values
    content {
      name  = set_sensitive.key
      value = set_sensitive.value
    }
  }

  atomic          = var.atomic_deployment
  cleanup_on_fail = var.cleanup_on_fail
  timeout         = var.timeout_seconds
  wait            = var.wait_for_resources

  depends_on = [
    kubernetes_namespace.monitoring,
    # Potentially depends on IAM roles for service accounts if IRSA is used by Prometheus components
    # e.g., for Prometheus remote write to AWS Managed Prometheus or CloudWatch
  ]

  # REQ-17-005: Metrics collection (Prometheus) and Visualization (Grafana)
  # REQ-17-006: Alerting logic (Prometheus rules, Alertmanager)
}

# Note: Alert rules are often defined as PrometheusRule CRDs.
# These can be included in the helm_values or deployed separately using kubernetes_manifest
# or files in a directory (via kubectl_manifest data source and kubernetes_manifest resource).
# See alert_rules.tf if it's part of this module for specific rule definitions.