# environments/staging/main.tf
# Purpose: Define and orchestrate all infrastructure resources for the staging environment.
# LogicDescription: Calls shared infrastructure modules with configurations specific to 'staging'.
# Parameters often mirror production but with potentially scaled-down resources or specific testing configurations.
# Integrates with staging-specific secrets and monitoring.
# ImplementedFeatures: Staging VPC Setup, Staging EKS Cluster, Staging RDS Instances, Staging Monitoring & Alerting Setup
# RequirementIds: REQ-8-011, REQ-17-005, REQ-17-006, REQ-17-007, REQ-17-008, REQ-17-009

# Global/Shared Variables (assumed from root or injected)
variable "aws_region" {}
variable "project_name" {}
variable "organization_name" {}
variable "default_tags" {
  type    = map(string)
  default = {}
}

# Environment specific variables (defined in environments/staging/variables.tf)
variable "vpc_cidr" {}
variable "public_subnet_cidrs" {}
variable "private_subnet_cidrs" {}
variable "eks_cluster_version" {}
variable "eks_node_instance_type" {}
variable "eks_node_min_size" {}
variable "eks_node_max_size" {}
variable "eks_node_desired_size" {}
variable "rds_engine_version" {}
variable "rds_instance_class" {}
variable "rds_instances_count" {}
variable "rds_backup_retention_period" {}

variable "environment_name" {
  description = "Name of the environment"
  type        = string
  default     = "staging"
}

locals {
  env_tags = merge(
    var.default_tags,
    {
      "Environment" = var.environment_name,
      "Project"     = var.project_name
    }
  )
  resource_prefix = "${var.project_name}-${var.environment_name}"
}

# --- Networking ---
module "vpc" {
  source = "../../modules/networking/vpc"

  vpc_cidr_block      = var.vpc_cidr
  public_subnet_cidrs = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones  = slice(data.aws_availability_zones.available.names, 0, 2) # Staging might use 2 AZs
  enable_nat_gateway  = true
  single_nat_gateway  = false # Staging might use redundant NAT Gateways if closer to prod setup

  tags = local.env_tags
}

# --- Secrets Management (REQ-8-011) ---
module "secrets_manager" {
  source = "../../modules/security/secrets_manager"

  secrets_to_create = {
    rds_master_password = {
      description             = "Master password for RDS instance in ${var.environment_name}"
      recovery_window_in_days = 7 # Short recovery for staging
      generate_random_password = {
        length           = 16
        special_characters = true
      }
    }
    # Add other secrets as needed for staging
  }
  common_tags = local.env_tags
}

# --- Database (RDS Aurora PostgreSQL) ---
module "rds_aurora_postgresql" {
  source = "../../modules/database/rds_aurora"

  cluster_identifier      = "${local.resource_prefix}-aurora-pg"
  engine                  = "aurora-postgresql"
  engine_version          = var.rds_engine_version
  instance_class          = var.rds_instance_class
  instances_count         = var.rds_instances_count # Staging might have 2 instances for some HA testing
  publicly_accessible     = false
  vpc_id                  = module.vpc.vpc_id
  subnet_ids              = module.vpc.private_subnet_ids
  db_name                 = "${var.project_name}_staging_db"
  master_username         = "adminuser"
  master_password_secret_arn = module.secrets_manager.secrets["rds_master_password"].arn
  backup_retention_period = var.rds_backup_retention_period # e.g., 15 days for staging
  skip_final_snapshot     = false # Staging might keep final snapshot
  storage_encrypted       = true
  deletion_protection     = false # Staging might be deletable

  # Monitoring & Alerting for RDS (REQ-17-007)
  enable_cloudwatch_alarms = true
  alarm_sns_topic_arn      = module.sns_topics.topic_arns["critical_alerts_staging"]

  tags = local.env_tags
}

# --- Kubernetes (EKS) ---
module "eks_cluster" {
  source = "../../modules/kubernetes/eks_cluster"

  cluster_name    = "${local.resource_prefix}-eks"
  cluster_version = var.eks_cluster_version
  vpc_id          = module.vpc.vpc_id
  subnet_ids      = module.vpc.private_subnet_ids

  managed_node_groups = {
    general_purpose_staging = {
      instance_types = [var.eks_node_instance_type]
      min_size       = var.eks_node_min_size
      max_size       = var.eks_node_max_size
      desired_size   = var.eks_node_desired_size
      disk_size      = 50
    }
  }

  # Secrets integration (REQ-8-011)
  enable_external_secrets_operator = true
  external_secrets_iam_role_name   = "${local.resource_prefix}-eso-role"

  # APM Configuration (REQ-17-005)
  enable_apm_tooling = true

  # Cluster Alerting Configuration (REQ-17-006, REQ-17-007)
  enable_cluster_alerting_components = true

  tags = local.env_tags
}

# --- Monitoring & Alerting (Prometheus Stack on EKS - REQ-17-005, REQ-17-006, REQ-17-007) ---
module "prometheus_stack" {
  source = "../../modules/monitoring/prometheus_stack"
  depends_on = [module.eks_cluster]

  eks_cluster_name         = module.eks_cluster.cluster_name
  eks_oidc_provider_arn    = module.eks_cluster.oidc_provider_arn
  eks_cluster_endpoint     = module.eks_cluster.cluster_endpoint
  eks_cluster_ca_certificate = module.eks_cluster.cluster_certificate_authority_data
  
  namespace                = "monitoring-staging"
  grafana_admin_password_secret_name = "grafana-admin-password-${var.environment_name}"
  grafana_pvc_storage_class = "gp2"
  grafana_pvc_size          = "5Gi"

  prometheus_pvc_storage_class = "gp2"
  prometheus_pvc_size          = "10Gi"

  alertmanager_config_secret_name = "alertmanager-config-${var.environment_name}"
  # Configure Alertmanager to route to staging SNS topics

  alert_rules_enabled = true
  tags = local.env_tags
}

# --- Alerting SNS Topics (REQ-17-009) ---
module "sns_topics" {
  source = "../../modules/alerting/sns_topics"

  topic_names_with_subscriptions = {
    "critical_alerts_staging" = [{ protocol = "email", endpoint = "staging-alerts-critical@example.com" }],
    "warning_alerts_staging"  = [{ protocol = "email", endpoint = "staging-alerts-warning@example.com" }],
    "security_alerts_staging" = [{ protocol = "email", endpoint = "staging-security-alerts@example.com" }], # REQ-17-008
    "backup_alerts_staging"   = [{ protocol = "email", endpoint = "staging-backup-alerts@example.com" }]   # REQ-17-008
  }
  allowed_publishers = [
    "cloudwatch.amazonaws.com",
    "events.amazonaws.com",
  ]
  tags = local.env_tags
}

# --- Backup (AWS Backup - REQ-17-008 for failure alerts) ---
module "aws_backup" {
  source = "../../modules/backup_restore/aws_backup"

  backup_vault_name = "${local.resource_prefix}-backup-vault"
  backup_plan_name  = "${local.resource_prefix}-daily-backup-plan"
  backup_schedule   = "cron(0 4 * * ? *)" # Daily at 4 AM UTC for staging
  backup_tags_selection = {
    "staging-backup" = "true"
  }
  cold_storage_after_days = 90 # Longer than dev, shorter than prod
  delete_after_days       = 180

  enable_backup_alerts = true
  backup_alerts_sns_topic_arn = module.sns_topics.topic_arns["backup_alerts_staging"]

  tags = local.env_tags
}

# --- Security (GuardDuty - REQ-17-008 for findings) ---
module "guardduty" {
  source = "../../modules/security/guardduty"

  enable_guardduty               = true
  publish_findings_to_s3       = true # Enable for staging
  s3_bucket_for_findings_name  = "${local.resource_prefix}-guardduty-findings" # This implies an S3 module or resource
  enable_eventbridge_alerts    = true
  findings_sns_topic_arn       = module.sns_topics.topic_arns["security_alerts_staging"]
  guardduty_finding_severities = ["LOW", "MEDIUM", "HIGH"] # Wider range for staging

  tags = local.env_tags
}

# --- AWS Config Rules ---
module "s3_public_access_blocked_rule_staging" {
  source = "../../policies/aws_config_rules/ensure_s3_public_access_blocked"
  rule_name = "${local.resource_prefix}-s3-public-access-blocked"
}


# --- Data Sources ---
data "aws_availability_zones" "available" {}

# --- Outputs ---
output "staging_vpc_id" {
  description = "ID of the staging VPC"
  value       = module.vpc.vpc_id
}

output "staging_eks_cluster_name" {
  description = "Name of the staging EKS cluster"
  value       = module.eks_cluster.cluster_name
}

output "staging_rds_cluster_endpoint" {
  description = "Endpoint for the staging RDS Aurora cluster"
  value       = module.rds_aurora_postgresql.cluster_endpoint
  sensitive   = true
}