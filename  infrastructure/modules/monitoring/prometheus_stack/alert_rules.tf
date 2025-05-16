# Purpose: Codify alerting conditions for various system metrics monitored by Prometheus.
# LogicDescription: Contains Prometheus alerting rules defined as 'PrometheusRule' Kubernetes Custom Resources.
# REQ-17-007: Define alert conditions for application and infrastructure metrics.
# REQ-17-008: Define alerts for infrastructure failures and integration failures.

variable "prometheus_operator_namespace" {
  description = "Namespace where Prometheus Operator and Alertmanager are running."
  type        = string
  default     = "monitoring" # Common default for kube-prometheus-stack
}

variable "default_alert_severity_label" {
  description = "Default severity label for alerts (e.g., critical, warning)."
  type        = string
  default     = "critical"
}

variable "default_alert_for_duration" {
  description = "Default duration for which a condition must be true before an alert fires."
  type        = string
  default     = "5m"
}

# --- Kubernetes Cluster Alerts ---
resource "kubernetes_manifest" "kube_node_alerts" {
  provider = kubernetes # Assumes kubernetes provider is configured

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "kube-node-alerts"
      namespace = var.prometheus_operator_namespace
      labels = {
        role = "alert-rules"
        app  = "prometheus-operator" # Or your specific app label for Prometheus
      }
    }
    spec = {
      groups = [
        {
          name = "kubernetes-node-status"
          rules = [
            {
              alert = "KubeNodeNotReady"
              expr  = <<-EOT
                kube_node_status_condition{condition="Ready",status="true"} == 0
              EOT
              for    = var.default_alert_for_duration
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "Node {{ $labels.node }} is not ready."
                description = "The Kubernetes node {{ $labels.node }} has been unready for more than ${var.default_alert_for_duration}."
              }
            },
            {
              alert = "KubeNodeHighCpuUtilization"
              expr  = <<-EOT
                (sum(rate(node_cpu_seconds_total{mode!="idle"}[2m])) by (instance) / sum(rate(node_cpu_seconds_total[2m])) by (instance)) * 100 > 85
              EOT
              for    = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Node {{ $labels.instance }} high CPU utilization."
                description = "Node {{ $labels.instance }} CPU utilization is above 85% for 15 minutes."
              }
            },
            {
              alert = "KubeNodeHighMemoryUtilization"
              expr  = <<-EOT
                (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 85
              EOT
              for    = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Node {{ $labels.instance }} high memory utilization."
                description = "Node {{ $labels.instance }} memory utilization is above 85% for 15 minutes."
              }
            },
            {
              alert = "KubeNodeDiskPressure"
              expr  = <<-EOT
                kube_node_status_condition{condition="DiskPressure",status="true"} == 1
              EOT
              for    = var.default_alert_for_duration
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Node {{ $labels.node }} has disk pressure."
                description = "The Kubernetes node {{ $labels.node }} is under disk pressure."
              }
            },
          ]
        }
      ]
    }
  }
}

# --- Kubernetes Pod/Container Alerts ---
resource "kubernetes_manifest" "kube_pod_alerts" {
  provider = kubernetes

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "kube-pod-alerts"
      namespace = var.prometheus_operator_namespace
      labels = {
        role = "alert-rules"
        app  = "prometheus-operator"
      }
    }
    spec = {
      groups = [
        {
          name = "kubernetes-pod-status"
          rules = [
            {
              alert = "KubePodCrashLooping"
              expr  = <<-EOT
                rate(kube_pod_container_status_restarts_total[15m]) * 60 * 5 > 0
              EOT
              for    = "10m" # Alert if crash looping persists for 10 minutes
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "Pod {{ $labels.namespace }}/{{ $labels.pod }} ({{ $labels.container }}) is crash looping."
                description = "Pod {{ $labels.namespace }}/{{ $labels.pod }} container {{ $labels.container }} has been restarting frequently."
              }
            },
            {
              alert = "KubePodNotReady"
              expr  = <<-EOT
                sum by (namespace, pod) (kube_pod_status_phase{phase=~"Pending|Unknown"}) > 0
              EOT
              for    = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Pod {{ $labels.namespace }}/{{ $labels.pod }} is not ready."
                description = "Pod {{ $labels.namespace }}/{{ $labels.pod }} has been in Pending/Unknown state for more than 15 minutes."
              }
            },
            # Add more pod/container alerts like high CPU/Memory usage if needed
            # These often depend on cAdvisor metrics collected by node-exporter or kubelet
            {
              alert = "KubeContainerHighCpuUsage"
              expr  = <<-EOT
                sum(rate(container_cpu_usage_seconds_total{image!="", container!="POD"}[5m])) by (namespace, pod, container) / sum(container_spec_cpu_quota{image!="", container!="POD"}/container_spec_cpu_period{image!="", container!="POD"}) by (namespace, pod, container) * 100 > 80
              EOT
              for    = "10m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Container {{ $labels.namespace }}/{{ $labels.pod }}/{{ $labels.container }} high CPU usage."
                description = "Container {{ $labels.namespace }}/{{ $labels.pod }}/{{ $labels.container }} CPU usage is above 80% of its quota for 10 minutes."
              }
            },
          ]
        }
      ]
    }
  }
}

# --- Application Specific Alerts (Example) ---
resource "kubernetes_manifest" "application_http_alerts" {
  provider = kubernetes

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "app-http-alerts"
      namespace = var.prometheus_operator_namespace
      labels = {
        role = "alert-rules"
        app  = "prometheus-operator"
      }
    }
    spec = {
      groups = [
        {
          name = "application-http-status"
          rules = [
            {
              alert = "HighHttp5xxErrorRate"
              # Assuming http_requests_total metric with 'code' and 'job'/'service' labels
              expr  = <<-EOT
                sum(rate(http_requests_total{code=~"5.."}[5m])) by (job, service, namespace) / sum(rate(http_requests_total[5m])) by (job, service, namespace) * 100 > 5
              EOT
              for    = var.default_alert_for_duration
              labels = {
                severity = var.default_alert_severity_label
              }
              annotations = {
                summary     = "High HTTP 5xx error rate for {{ $labels.job }}/{{ $labels.service }} in {{ $labels.namespace }}."
                description = "{{ $labels.job }}/{{ $labels.service }} in {{ $labels.namespace }} is experiencing more than 5% HTTP 5xx errors."
              }
            },
            {
              alert = "HighHttpApiLatency"
              # Assuming http_request_duration_seconds_bucket metric
              expr  = <<-EOT
                histogram_quantile(0.99, sum(rate(http_request_duration_seconds_bucket[5m])) by (le, job, service, namespace)) > 1
              EOT
              for    = var.default_alert_for_duration
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "High API Latency (p99 > 1s) for {{ $labels.job }}/{{ $labels.service }} in {{ $labels.namespace }}."
                description = "The 99th percentile API request latency for {{ $labels.job }}/{{ $labels.service }} in {{ $labels.namespace }} is over 1 second."
              }
            }
          ]
        }
      ]
    }
  }
}

# --- Service Unavailability / Blackbox Exporter Alerts (Example) ---
# This requires Blackbox Exporter to be deployed and configured
/*
resource "kubernetes_manifest" "blackbox_exporter_alerts" {
  provider = kubernetes

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "blackbox-alerts"
      namespace = var.prometheus_operator_namespace
      labels = {
        role = "alert-rules"
        app  = "prometheus-operator"
      }
    }
    spec = {
      groups = [
        {
          name = "service-availability"
          rules = [
            {
              alert = "BlackboxProbeFailed"
              expr  = "probe_success == 0"
              for    = "2m" # Alert if probe fails for 2 minutes (adjust based on probe interval)
              labels = {
                severity = "critical"
              }
              annotations = {
                summary     = "Service probe failed for target {{ $labels.instance }} (module {{ $labels.module }})."
                description = "The blackbox probe for {{ $labels.instance }} (module {{ $labels.module }}) has been failing for more than 2 minutes."
              }
            },
            {
              alert = "BlackboxSSLCertificateWillExpireSoon"
              expr  = "probe_ssl_earliest_cert_expiry - time() < 86400 * 14" # 14 days
              for   = "1h"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "SSL certificate for {{ $labels.instance }} will expire in less than 14 days."
                description = "The SSL certificate for {{ $labels.instance }} is due to expire on {{ humanizeTimestamp ($labels.probe_ssl_earliest_cert_expiry) }}."
              }
            }
          ]
        }
      ]
    }
  }
}
*/

# --- Integration Failure Alerts (Example for RabbitMQ) ---
# This assumes metrics like 'rabbitmq_queue_messages_ready' are available.
/*
resource "kubernetes_manifest" "rabbitmq_alerts" {
  provider = kubernetes

  manifest = {
    apiVersion = "monitoring.coreos.com/v1"
    kind       = "PrometheusRule"
    metadata = {
      name      = "rabbitmq-alerts"
      namespace = var.prometheus_operator_namespace
      labels = {
        role = "alert-rules"
        app  = "prometheus-operator"
      }
    }
    spec = {
      groups = [
        {
          name = "rabbitmq-queue-status"
          rules = [
            {
              alert = "RabbitMQHighQueueDepth"
              expr  = "rabbitmq_queue_messages_ready{queue!~\"^amq\\.gen.*\"} > 1000" # Exclude auto-generated queues
              for    = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "RabbitMQ queue {{ $labels.queue }} in vhost {{ $labels.vhost }} has high depth."
                description = "Queue {{ $labels.queue }} (vhost {{ $labels.vhost }}) has over 1000 messages ready for more than 15 minutes."
              }
            },
            {
              alert = "RabbitMQTooManyUnacknowledgedMessages"
              expr  = "rabbitmq_queue_messages_unacknowledged{queue!~\"^amq\\.gen.*\"} > 500"
              for   = "10m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "RabbitMQ queue {{ $labels.queue }} in vhost {{ $labels.vhost }} has too many unacknowledged messages."
                description = "Queue {{ $labels.queue }} (vhost {{ $labels.vhost }}) has over 500 unacknowledged messages for more than 10 minutes."
              }
            }
          ]
        }
      ]
    }
  }
}
*/