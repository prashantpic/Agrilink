# environments/dev/main.tf
# Purpose: Define and orchestrate all infrastructure resources for the development environment.
# LogicDescription: Calls shared infrastructure modules (e.g., VPC, EKS, RDS, S3, Secrets Manager, Prometheus Stack, SNS Topics, AWS Backup, GuardDuty)
# providing 'dev' specific configurations. Values are sourced from dev-specific .tfvars files and securely injected secrets.
# Implements resource provisioning according to dev environment needs, often with smaller instance sizes and fewer replicas.

# Provider configuration is assumed to be in the root or handled by the calling configuration.
# This file focuses on module instantiation for the 'dev' environment.

# --- Core Networking ---
module "vpc" {
  source = "../../modules/networking/vpc"

  vpc_cidr_block      = var.vpc_cidr_block
  public_subnet_cidrs = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones  = var.availability_zones
  enable_nat_gateway  = var.enable_nat_gateway
  single_nat_gateway  = var.single_nat_gateway
  project_name        = var.project_name
  environment         = var.environment_name
  common_tags         = local.common_tags
}

# --- EKS Cluster ---
module "eks_cluster" {
  source = "../../modules/kubernetes/eks_cluster"

  cluster_name                  = "${var.project_name}-${var.environment_name}-eks"
  cluster_version               = var.eks_cluster_version
  vpc_id                        = module.vpc.vpc_id
  private_subnet_ids            = module.vpc.private_subnet_ids
  control_plane_subnet_ids      = module.vpc.private_subnet_ids # Typically private for control plane communication
  node_group_instance_types     = var.eks_node_group_instance_types
  node_group_desired_size       = var.eks_node_group_desired_size
  node_group_min_size           = var.eks_node_group_min_size
  node_group_max_size           = var.eks_node_group_max_size
  project_name                  = var.project_name
  environment                   = var.environment_name
  aws_region                    = var.aws_region
  common_tags                   = local.common_tags
  enable_external_secrets       = true # REQ-8-011
  external_secrets_iam_role_name = "${var.project_name}-${var.environment_name}-eso-role"
  enable_prometheus_stack_dependency_config = true # For APM/Alerting REQ-17-005, REQ-17-006
  # APM related configurations can be passed here if the module supports it
}

# --- Database (RDS Aurora PostgreSQL) ---
module "rds_aurora_postgresql" {
  source = "../../modules/database/rds_aurora"

  cluster_identifier      = "${var.project_name}-${var.environment_name}-aurora-pg"
  engine                  = "aurora-postgresql"
  engine_version          = var.rds_engine_version
  instance_class          = var.rds_instance_class
  instances_count         = var.rds_instances_count # Typically 1 for dev, 2+ for staging/prod
  vpc_id                  = module.vpc.vpc_id
  db_subnet_group_name    = module.vpc.database_subnet_group_name # Assuming VPC module creates this or provides subnets
  vpc_security_group_ids  = [module.vpc.default_security_group_id] # Example, ideally a dedicated SG
  db_name                 = var.rds_db_name
  master_username         = var.rds_master_username
  # Master password should be sourced from a secure secrets management, e.g. var.rds_master_password injected via CI/CD
  master_password         = var.rds_master_password
  backup_retention_period = var.rds_backup_retention_period
  skip_final_snapshot     = true # For dev
  project_name            = var.project_name
  environment             = var.environment_name
  common_tags             = local.common_tags
  # Enable monitoring alerts REQ-17-007
  enable_monitoring_alerts = true
  alarm_sns_topic_arn      = module.sns_topics.alert_topics["critical_alerts_topic"].arn
}

# --- Secrets Management (AWS Secrets Manager for App Secrets) ---
# REQ-8-011
module "application_secrets" {
  source = "../../modules/security/secrets_manager"

  secrets_config = {
    # Example: Create a secret for RDS master password if not directly set,
    # or for other application secrets.
    # "rds_master_credentials" = {
    #   description             = "RDS master credentials for dev environment"
    #   recovery_window_in_days = 7
    #   secret_string           = jsonencode({ username = var.rds_master_username, password = var.rds_master_password }) # If password is provided
    #   # OR use random password generation if var.rds_master_password is not provided
    #   # generate_random_password = { length = 16, special = true }
    #   tags                    = local.common_tags
    # },
    "myapplication/dev/api_key" = {
      description             = "API Key for My Application in Dev"
      recovery_window_in_days = 0 # No recovery for dev if desired
      secret_string           = "dev-dummy-api-key-to-be-replaced" # Placeholder
      tags                    = local.common_tags
    }
  }

  common_tags = local.common_tags
}

# --- Monitoring & Alerting (Prometheus Stack on EKS) ---
# REQ-17-005, REQ-17-006, REQ-17-007
module "prometheus_stack" {
  source = "../../modules/monitoring/prometheus_stack"

  eks_cluster_name          = module.eks_cluster.cluster_name
  eks_oidc_provider_arn     = module.eks_cluster.oidc_provider_arn
  eks_cluster_endpoint      = module.eks_cluster.cluster_endpoint
  eks_cluster_ca_certificate = module.eks_cluster.cluster_ca_certificate # For Helm provider

  namespace                 = "monitoring"
  create_namespace          = true
  grafana_admin_password    = var.grafana_admin_password # Should be a securely managed variable
  alertmanager_config = {
    global = {
      resolve_timeout = "5m"
    }
    route = {
      group_by        = ["job", "alertname", "severity"]
      group_wait      = "30s"
      group_interval  = "5m"
      repeat_interval = "1h"
      receiver        = "default-receiver"
      routes = [
        {
          receiver = "critical-alerts-sns"
          matchers = ["severity=\"critical\""]
        },
        {
          receiver = "warning-alerts-sns"
          matchers = ["severity=\"warning\""]
        }
      ]
    }
    receivers = [
      {
        name = "default-receiver"
        # Add a default receiver if needed, e.g., null or a dev-specific channel
      },
      {
        name = "critical-alerts-sns"
        webhook_configs = [{
          url = "http://alertmanager-sns-webhook.monitoring.svc.cluster.local:3000/sns?topic_arn=${module.sns_topics.alert_topics["critical_alerts_topic"].arn}" # Example internal URL for sns-webhook
          send_resolved = true
        }]
      },
      {
        name = "warning-alerts-sns"
        webhook_configs = [{
          url = "http://alertmanager-sns-webhook.monitoring.svc.cluster.local:3000/sns?topic_arn=${module.sns_topics.alert_topics["warning_alerts_topic"].arn}" # Example
          send_resolved = true
        }]
      }
    ]
  }
  # Example alert rules definition (can be more complex and passed as a map/list of objects)
  # REQ-17-007, REQ-17-008
  prometheus_rules = [
    {
      name = "dev-cluster-rules"
      groups = [
        {
          name = "kubernetes-apps"
          rules = [
            {
              alert = "KubePodCrashLooping"
              expr  = "kube_pod_container_status_restarts_total > 5"
              for   = "15m"
              labels = {
                severity = "warning"
              }
              annotations = {
                summary     = "Pod {{ $labels.namespace }}/{{ $labels.pod }} is crash looping."
                description = "{{ $labels.pod }} in {{ $labels.namespace }} has restarted {{ $value }} times in the last 15 minutes."
              }
            }
          ]
        }
      ]
    }
  ]
  common_tags = local.common_tags
}

# --- SNS Topics for Alerting ---
# REQ-17-006, REQ-17-009
module "sns_topics" {
  source = "../../modules/alerting/sns_topics"

  topic_names_with_config = {
    "critical_alerts_topic" = {
      display_name = "${var.project_name}-${var.environment_name}-critical-alerts"
      subscriptions = [
        { protocol = "email", endpoint = var.critical_alerts_email }
      ]
    },
    "warning_alerts_topic" = {
      display_name = "${var.project_name}-${var.environment_name}-warning-alerts"
      subscriptions = [
        { protocol = "email", endpoint = var.warning_alerts_email }
      ]
    },
    "backup_failure_alerts_topic" = { # REQ-17-008
      display_name = "${var.project_name}-${var.environment_name}-backup-failures"
      subscriptions = [
        { protocol = "email", endpoint = var.backup_alerts_email }
      ]
    },
    "security_alerts_topic" = { # REQ-17-008 (for GuardDuty)
      display_name = "${var.project_name}-${var.environment_name}-security-alerts"
      subscriptions = [
        { protocol = "email", endpoint = var.security_alerts_email }
      ]
    }
  }
  common_tags = local.common_tags
}

# --- AWS Backup ---
# REQ-17-008 (for backup failure alerts)
module "aws_backup" {
  source = "../../modules/backup_restore/aws_backup"

  vault_name          = "${var.project_name}-${var.environment_name}-backup-vault"
  backup_plan_name    = "${var.project_name}-${var.environment_name}-daily-backup-plan"
  iam_role_arn        = var.aws_backup_iam_role_arn # Must be created with appropriate permissions
  schedule_expression = "cron(0 5 ? * * *)"        # Daily at 5 AM UTC
  resource_assignments = {
    "rds_databases" = {
      resource_type_conditions = ["RDS"] # Backup all RDS instances
      # Can be more specific using tags or ARNs
      # selection_tags = [{ type = "STRINGEQUALS", key = "backup", value = "daily" }]
    }
  }
  # Enable alerting for backup failures
  enable_alerting     = true
  failure_sns_topic_arn = module.sns_topics.alert_topics["backup_failure_alerts_topic"].arn
  common_tags         = local.common_tags
}

# --- Security (AWS GuardDuty) ---
# REQ-17-008 (for security alerts)
module "guardduty" {
  source = "../../modules/security/guardduty"

  enable_guardduty         = true
  finding_publishing_frequency = "FIFTEEN_MINUTES" # For dev, could be longer for prod
  export_findings_to_s3    = false # Optional for dev
  # s3_bucket_name        = (if export_findings_to_s3 is true)

  # Enable alerting for GuardDuty findings
  enable_alerting          = true
  alert_sns_topic_arn      = module.sns_topics.alert_topics["security_alerts_topic"].arn
  # GuardDuty findings severity for alerts (e.g., HIGH, MEDIUM)
  alert_severity_threshold_gte = 7 # Corresponds to High severity
  common_tags              = local.common_tags
}

# --- Common Local Variables ---
locals {
  common_tags = merge(
    var.default_tags,
    {
      "Environment" = var.environment_name,
      "Project"     = var.project_name
      # Add other common tags here
    }
  )
}

# --- Outputs from the Dev Environment ---
output "vpc_id" {
  description = "The ID of the VPC."
  value       = module.vpc.vpc_id
}

output "eks_cluster_name" {
  description = "The name of the EKS cluster."
  value       = module.eks_cluster.cluster_name
}

output "eks_cluster_endpoint" {
  description = "The endpoint for the EKS cluster's API server."
  value       = module.eks_cluster.cluster_endpoint
}

output "rds_aurora_cluster_endpoint" {
  description = "The endpoint of the RDS Aurora cluster."
  value       = module.rds_aurora_postgresql.cluster_endpoint
}

output "rds_aurora_cluster_reader_endpoint" {
  description = "The reader endpoint of the RDS Aurora cluster."
  value       = module.rds_aurora_postgresql.cluster_reader_endpoint
}

output "critical_alerts_sns_topic_arn" {
  description = "ARN of the SNS topic for critical alerts."
  value       = module.sns_topics.alert_topics["critical_alerts_topic"].arn
}

output "application_secrets_arns" {
  description = "ARNs of the application secrets created in Secrets Manager."
  value       = module.application_secrets.secret_arns
}