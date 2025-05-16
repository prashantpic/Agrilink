# Purpose: Set up a comprehensive monitoring and alerting solution using the Prometheus stack on Kubernetes.
# LogicDescription: Deploys Prometheus for metrics collection and storage, Alertmanager for handling alerts,
# and Grafana for visualization. This is typically achieved by deploying the kube-prometheus-stack Helm chart.
# Configures persistent storage for Prometheus and Grafana, service discovery mechanisms, and basic Alertmanager routing.
# ImplementedFeatures: Prometheus Deployment, Alertmanager Deployment, Grafana Deployment,
# Persistent Storage Configuration, Service Discovery for Prometheus.
# RequirementIds: REQ-17-005, REQ-17-006

# Assumes Kubernetes and Helm providers are configured in the calling environment.
# The Kubernetes provider should be configured to point to the EKS cluster created by the eks_cluster module.

locals {
  chart_version = var.prometheus_stack_chart_version
  release_name  = "${var.project_name}-${var.environment}-prom-stack"
  namespace     = var.namespace
  tags          = var.tags
}

resource "kubernetes_namespace" "monitoring" {
  count = var.create_namespace ? 1 : 0
  metadata {
    name = local.namespace
    labels = merge(
      local.tags,
      {
        "name" = local.namespace
      }
    )
  }
}

resource "helm_release" "kube_prometheus_stack" {
  name       = local.release_name
  repository = var.prometheus_stack_helm_repo
  chart      = "kube-prometheus-stack"
  version    = local.chart_version
  namespace  = local.namespace

  # REQ-17-005: Deploy Prometheus, Alertmanager, Grafana
  # REQ-17-006: Deploy Alertmanager for handling alerts
  values = [
    yamlencode({
      # Global settings
      global = {
        # Common labels for all resources
        commonLabels = local.tags
      }

      # Prometheus configuration
      prometheus = {
        enabled = true
        prometheusSpec = {
          # Resource requests and limits
          resources = var.prometheus_resources
          # Persistent storage configuration
          storageSpec = {
            volumeClaimTemplate = {
              spec = {
                accessModes = [var.prometheus_pvc_access_modes]
                resources = {
                  requests = {
                    storage = var.prometheus_pvc_storage_size
                  }
                }
                storageClassName = var.prometheus_pvc_storage_class_name # Ensure this StorageClass exists
              }
            }
          }
          # How long to retain metrics
          retention = var.prometheus_retention_period
          # Service Monitor and Pod Monitor selectors
          serviceMonitorSelectorNilUsesHelmValues = false
          podMonitorSelectorNilUsesHelmValues     = false
          ruleSelectorNilUsesHelmValues           = false

          # Additional scrape configs can be added here or via PrometheusRule CRDs
          # additionalScrapeConfigs = [] 
        }
      }

      # Alertmanager configuration
      alertmanager = {
        enabled = true
        alertmanagerSpec = {
          resources = var.alertmanager_resources
          storage = { # Persistent storage for Alertmanager state
            volumeClaimTemplate = {
              spec = {
                accessModes = [var.alertmanager_pvc_access_modes]
                resources = {
                  requests = {
                    storage = var.alertmanager_pvc_storage_size
                  }
                }
                storageClassName = var.alertmanager_pvc_storage_class_name # Ensure this StorageClass exists
              }
            }
          }
        }
        # Basic Alertmanager configuration (receivers, routes)
        # More complex configs can be managed via a ConfigMap or the AlertmanagerConfig CRD
        config = {
          global = {
            resolve_timeout = "5m"
            # Example: Point to an SNS topic via a webhook or an AWS SNS receiver
            # This typically involves a separate Alertmanager receiver like `prometheus-alertmanager-sns-forwarder`
            # or a custom Lambda function. For simplicity, showing a webhook example.
            # This will be further configured by `alert_rules.tf` potentially setting up `PrometheusRule` CRDs.
            # And SNS topics from `sns_topics` module will be the ultimate destination.
          }
          route = {
            group_by = ["job", "alertname", "severity"]
            receiver = var.default_alertmanager_receiver_name # e.g., "default-receiver"
            routes   = var.alertmanager_routes # More specific routes can be added here
          }
          receivers = [
            {
              name = var.default_alertmanager_receiver_name
              # Example webhook configuration - replace with actual integration
              # webhook_configs = [{
              #   url = "http://<alert-processor-service>.<namespace>.svc.cluster.local:9099/alerts"
              #   send_resolved = true
              # }]
              # Example: If using an SNS receiver deployment.
              # This setup depends on the `sns_topics` module output (topic ARN) and an sns integration method
              # e.g. using https://github.com/prometheus-community/alertmanager-sns-receiver
              # or a custom webhook to lambda to SNS.
            }
            # Add more receivers as needed based on var.alertmanager_additional_receivers
          ]
          # templates = [] # For custom notification templates
        }
      }

      # Grafana configuration
      grafana = {
        enabled = true
        adminPassword = var.grafana_admin_password # Consider fetching from Secrets Manager
        resources = var.grafana_resources
        persistence = {
          enabled = true
          type    = "pvc"
          size    = var.grafana_pvc_storage_size
          accessModes = [var.grafana_pvc_access_modes]
          storageClassName = var.grafana_pvc_storage_class_name # Ensure this StorageClass exists
        }
        # For Grafana dashboards, they can be provisioned via ConfigMaps
        # dashboards.default.enabled: true (or custom providers)
        # sidecar.dashboards.enabled: true
        # sidecar.dashboards.searchNamespace: "ALL"
        # See grafana_dashboards/apm_dashboard.json for an example dashboard definition
        # that would be mounted via a ConfigMap.
        # This helm chart can automatically pick up dashboards from ConfigMaps with specific labels.
        # Or define dashboardsProvider.
        # Example:
        # dashboardsConfigMaps:
        #   my-dashboards: "my-dashboards-configmap-name"
      }

      # Kube-state-metrics configuration
      kubeStateMetrics = {
        enabled = true # REQ-17-005 (part of metrics collection)
        # resources = {} # Configure if needed
      }

      # Node-exporter configuration
      nodeExporter = {
        enabled = true # REQ-17-005 (part of metrics collection)
        # resources = {} # Configure if needed
      }
      
      # Prometheus Operator specific configurations
      prometheusOperator = {
        # resources = {} # Configure if needed
      }

      # Default rules provided by the chart (can be disabled if managing all rules externally)
      defaultRules = {
        create = true
        # rules = {} # To disable specific default rules
      }

      # Any additional custom values
      # customValues = var.additional_helm_values
    }),
    # Merging additional values provided by the user
    var.additional_helm_values_yaml != "" ? var.additional_helm_values_yaml : ""
  ]

  atomic = true # If set, the installation process purges chart on failure.
  # timeout = 300 # Timeout in seconds for the operation.

  depends_on = [
    kubernetes_namespace.monitoring,
    # Potentially depends on storage classes being available if not using default
  ]

  # Set specific provider if multiple Kubernetes providers are configured
  # provider = kubernetes.eks_cluster # Example
}