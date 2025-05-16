# Description: Defines specific alert rules for Prometheus.
# Purpose: Codify alerting conditions for various system metrics monitored by Prometheus.
# Requirements: REQ-17-007, REQ-17-008

variable "prometheus_operator_prometheus_release" {
  description = "Name of the Prometheus custom resource (e.g., 'prometheus-kube-prometheus-prometheus'). Used for labels if Prometheus Operator needs them."
  type        = string
  default     = "prometheus" # This should match the Prometheus CR name
}

variable "prometheus_rules_namespace" {
  description = "Namespace where PrometheusRule CRs are deployed. This should be the namespace monitored by Prometheus Operator."
  type        = string
  default     = "monitoring" # Default for kube-prometheus-stack
}

variable "tags" {
  description = "A map of tags to assign to Kubernetes manifest resources if supported (usually not directly, but good for logical grouping)."
  type        = map(string)
  default     = {}
}

# --- Generic Kubernetes Alert Rules ---
resource "kubernetes_manifest" "kube_node_alerts" {
  provider = kubernetes # Assuming kubernetes provider is configured

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "kube-node-alerts"
      namespace = var.prometheus_rules_namespace
      labels = {
        prometheus = var.prometheus_operator_prometheus_release # Label for Prometheus Operator to discover
        role       = "alert-rules"
      }
    }
    spec = {
      groups = [
        {
          name = "kubernetes-node-alerts"
          rules = [
            {
              alert = "KubeNodeNotReady"
              expr  = "kube_node_status_condition{condition='Ready',status='true'} == 0"
              for   = "10m"
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "Node {{ $labels.node }} is not ready."
                description = "The node {{ $labels.node }} has been unready for more than 10 minutes."
              }
            },
            {
              alert = "KubeNodeHighCpuLoad"
              expr  = "(100 - (avg by (instance) (rate(node_cpu_seconds_total{mode='idle'}[5m])) * 100)) > 85"
              for   = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Node {{ $labels.instance }} has high CPU load."
                description = "Node {{ $labels.instance }} CPU load is above 85% for 15 minutes. Current value: {{ $value }}%."
              }
            },
            {
              alert = "KubeNodeHighMemoryUsage"
              expr  = "(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100 > 85"
              for   = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Node {{ $labels.instance }} has high memory usage."
                description = "Node {{ $labels.instance }} memory usage is above 85% for 15 minutes. Current value: {{ $value }}%."
              }
            },
            {
              alert = "KubeNodeDiskPressure"
              expr  = "kube_node_status_condition{condition='DiskPressure',status='true'} == 1"
              for   = "5m"
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "Node {{ $labels.node }} is under disk pressure."
                description = "The node {{ $labels.node }} is reporting DiskPressure."
              }
            },
            {
              alert = "KubeNodeFilesystemFull"
              expr  = "node_filesystem_avail_bytes{mountpoint!~\".*pod.*\"} / node_filesystem_size_bytes{mountpoint!~\".*pod.*\"} * 100 < 10"
              for   = "5m"
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "Node {{ $labels.instance }} filesystem full on {{ $labels.mountpoint }}."
                description = "Filesystem {{ $labels.mountpoint }} on node {{ $labels.instance }} has less than 10% space available. Current value: {{ $value }}%."
              }
            }
          ]
        }
      ]
    }
  }
}

resource "kubernetes_manifest" "kube_pod_alerts" {
  provider = kubernetes

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "kube-pod-alerts"
      namespace = var.prometheus_rules_namespace
      labels = {
        prometheus = var.prometheus_operator_prometheus_release
        role       = "alert-rules"
      }
    }
    spec = {
      groups = [
        {
          name = "kubernetes-pod-alerts"
          rules = [
            {
              alert = "KubePodCrashLooping"
              expr  = "rate(kube_pod_container_status_restarts_total[15m]) * 60 * 5 > 0" # More than 0 restarts in 5 minutes, sustained over 15m sampling
              for   = "10m" # Pod has been crashlooping for 10 minutes
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "Pod {{ $labels.namespace }}/{{ $labels.pod }} is crash looping."
                description = "Container {{ $labels.container }} in pod {{ $labels.namespace }}/{{ $labels.pod }} has been restarting frequently."
              }
            },
            {
              alert = "KubePodNotReady"
              # This rule identifies pods that are part of a Deployment/StatefulSet/DaemonSet and are not ready.
              expr  = "sum by (namespace, pod, owner_kind, owner_name) (kube_pod_status_ready{condition='false'}) > 0 and on(namespace, pod) kube_pod_owner{owner_kind!='Job'}"
              for   = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Pod {{ $labels.namespace }}/{{ $labels.pod }} is not ready."
                description = "Pod {{ $labels.namespace }}/{{ $labels.pod }} (owned by {{ $labels.owner_kind }}/{{ $labels.owner_name }}) has been not ready for 15 minutes."
              }
            },
            {
              alert = "KubeContainerHighCpuUsage"
              # Example: Alert if a container uses more than 80% of its CPU limit for 10 minutes. Requires CPU limits to be set.
              expr  = "(sum(rate(container_cpu_usage_seconds_total{container!='', image!=''}[5m])) by (namespace, pod, container) / sum(kube_pod_container_resource_limits{resource='cpu'}) by (namespace, pod, container) * 100) > 80"
              for   = "10m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Container {{ $labels.container }} in {{ $labels.namespace }}/{{ $labels.pod }} has high CPU usage."
                description = "Container {{ $labels.container }} in pod {{ $labels.namespace }}/{{ $labels.pod }} is using {{ $value | printf \"%.2f\" }}% of its CPU limit."
              }
            },
            {
              alert = "KubeContainerHighMemoryUsage"
              # Example: Alert if a container uses more than 80% of its Memory limit. Requires memory limits to be set.
              expr  = "(sum(container_memory_working_set_bytes{container!='', image!='' }) by (namespace, pod, container) / sum(kube_pod_container_resource_limits{resource='memory'}) by (namespace, pod, container) * 100) > 80"
              for   = "10m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Container {{ $labels.container }} in {{ $labels.namespace }}/{{ $labels.pod }} has high memory usage."
                description = "Container {{ $labels.container }} in pod {{ $labels.namespace }}/{{ $labels.pod }} is using {{ $value | printf \"%.2f\" }}% of its memory limit."
              }
            }
          ]
        }
      ]
    }
  }
}

# --- Application Specific Alert Rules (Example) ---
# Users should add their own application-specific rules here.
resource "kubernetes_manifest" "app_error_rate_alerts" {
  provider = kubernetes

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "app-error-rate-alerts"
      namespace = var.prometheus_rules_namespace
      labels = {
        prometheus = var.prometheus_operator_prometheus_release
        role       = "alert-rules"
      }
    }
    spec = {
      groups = [
        {
          name = "application-error-alerts"
          rules = [
            {
              alert = "HighHttp5xxErrorRate"
              # Assumes http_requests_total metric with 'code' and 'handler'/'service' labels
              expr  = "sum(rate(http_requests_total{code=~'5..'}[5m])) by (job, service, handler) / sum(rate(http_requests_total[5m])) by (job, service, handler) * 100 > 5"
              for   = "10m"
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "High HTTP 5xx error rate for {{ $labels.service }} (handler: {{ $labels.handler }})."
                description = "{{ $labels.service }} (handler: {{ $labels.handler }}) is experiencing {{ $value | printf \"%.2f\" }}% 5xx errors over the last 10 minutes."
              }
            },
            {
              alert = "HighHttp4xxErrorRate"
              expr  = "sum(rate(http_requests_total{code=~'4..', code!='401', code!='403', code!='404'}[5m])) by (job, service, handler) / sum(rate(http_requests_total[5m])) by (job, service, handler) * 100 > 10"
              for   = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "High HTTP 4xx error rate (excluding 401/403/404) for {{ $labels.service }} (handler: {{ $labels.handler }})."
                description = "{{ $labels.service }} (handler: {{ $labels.handler }}) is experiencing {{ $value | printf \"%.2f\" }}% 4xx errors over the last 15 minutes."
              }
            }
          ]
        }
      ]
    }
  }
}

# --- Integration Failure Alerts (Example for RabbitMQ) ---
# This requires RabbitMQ metrics to be scraped by Prometheus.
resource "kubernetes_manifest" "rabbitmq_alerts" {
  provider = kubernetes

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "rabbitmq-alerts"
      namespace = var.prometheus_rules_namespace
      labels = {
        prometheus = var.prometheus_operator_prometheus_release
        role       = "alert-rules"
      }
    }
    spec = {
      groups = [
        {
          name = "rabbitmq-queue-alerts"
          rules = [
            {
              alert = "RabbitMqTooManyUnackMessages"
              expr  = "rabbitmq_queue_messages_unacked > 1000" # Adjust threshold as needed
              for   = "10m"
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "RabbitMQ queue {{ $labels.queue }} in vhost {{ $labels.vhost }} has too many unacknowledged messages."
                description = "Queue {{ $labels.queue }} has {{ $value }} unacknowledged messages for more than 10 minutes."
              }
            },
            {
              alert = "RabbitMqTooManyReadyMessages"
              expr  = "rabbitmq_queue_messages_ready > 5000" # Adjust threshold
              for   = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "RabbitMQ queue {{ $labels.queue }} in vhost {{ $labels.vhost }} has many ready messages (potential consumer issue)."
                description = "Queue {{ $labels.queue }} has {{ $value }} ready messages for more than 15 minutes."
              }
            },
            {
              alert = "RabbitMqNoConsumers"
              # Assumes rabbitmq_queue_consumers metric exists.
              expr  = "rabbitmq_queue_messages_ready > 100 and rabbitmq_queue_consumers == 0"
              for   = "5m"
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "RabbitMQ queue {{ $labels.queue }} has messages but no consumers."
                description = "Queue {{ $labels.queue }} in vhost {{ $labels.vhost }} has {{ $value }} messages ready but no consumers for 5 minutes."
              }
            }
          ]
        }
      ]
    }
  }
}

# Requirement REQ-17-008: Alerts for system errors or integration failures.
# - KubePodCrashLooping, KubeNodeNotReady cover some system errors.
# - RabbitMq alerts cover an example integration failure.
# - Users should add more specific integration failure alerts based on metrics exposed by their integrations.
#   For example, database connection pool exhaustion, errors calling third-party APIs, etc.