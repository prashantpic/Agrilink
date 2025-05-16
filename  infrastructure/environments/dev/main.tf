# environments/dev/main.tf
# Purpose: Define and orchestrate all infrastructure resources for the development environment.
# LogicDescription: Calls shared infrastructure modules (e.g., VPC, EKS, RDS, S3, Secrets Manager, Prometheus Stack, SNS Topics, AWS Backup, GuardDuty)
# providing 'dev' specific configurations. Values are sourced from dev-specific .tfvars files and securely injected secrets.
# Implements resource provisioning according to dev environment needs, often with smaller instance sizes and fewer replicas.
# ImplementedFeatures: Dev VPC Setup, Dev EKS Cluster, Dev RDS Instances, Dev Monitoring & Alerting Setup, Dev Secrets Management Integration, Dev Backup Configuration
# RequirementIds: REQ-8-011, REQ-17-005, REQ-17-006, REQ-17-007, REQ-17-008, REQ-17-009

# Global/Shared Variables (assumed from root or injected)
variable "aws_region" {}
variable "project_name" {}
variable "organization_name" {}
variable "default_tags" {
  type    = map(string)
  default = {}
}

# Environment specific variables
variable "environment_name" {
  description = "Name of the environment (e.g., dev, staging, prod)"
  type        = string
  default     = "dev"
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
  availability_zones  = slice(data.aws_availability_zones.available.names, 0, 2) # Dev might use 2 AZs
  enable_nat_gateway  = true
  single_nat_gateway  = true # Cost saving for dev

  tags = local.env_tags
}

# --- Secrets Management (REQ-8-011) ---
module "secrets_manager" {
  source = "../../modules/security/secrets_manager"

  secrets_to_create = {
    rds_master_password = {
      description             = "Master password for RDS instance in ${var.environment_name}"
      recovery_window_in_days = 0 # No recovery for dev, or set to a low value
      generate_random_password = {
        length           = 16
        special_characters = true
      }
    }
    # Add other secrets as needed for dev
  }
  common_tags = local.env_tags
}

# --- Database (RDS Aurora PostgreSQL) ---
module "rds_aurora_postgresql" {
  source = "../../modules/database/rds_aurora"

  cluster_identifier      = "${local.resource_prefix}-aurora-pg"
  engine                  = "aurora-postgresql"
  engine_version          = var.rds_engine_version
  instance_class          = var.rds_instance_class # Dev specific instance class
  instances_count         = 1 # Single instance for dev
  publicly_accessible     = false
  vpc_id                  = module.vpc.vpc_id
  subnet_ids              = module.vpc.private_subnet_ids
  db_name                 = "${var.project_name}_dev_db"
  master_username         = "adminuser"
  master_password_secret_arn = module.secrets_manager.secrets["rds_master_password"].arn
  backup_retention_period = 7    # Shorter retention for dev
  skip_final_snapshot     = true # For dev, skip final snapshot
  storage_encrypted       = true
  deletion_protection     = false # Dev can be easily deleted

  # Monitoring & Alerting for RDS (REQ-17-007)
  enable_cloudwatch_alarms = true
  alarm_sns_topic_arn      = module.sns_topics.topic_arns["critical_alerts"] # Example topic

  tags = local.env_tags
}

# --- Kubernetes (EKS) ---
module "eks_cluster" {
  source = "../../modules/kubernetes/eks_cluster"

  cluster_name    = "${local.resource_prefix}-eks"
  cluster_version = var.eks_cluster_version
  vpc_id          = module.vpc.vpc_id
  subnet_ids      = module.vpc.private_subnet_ids # EKS control plane in private subnets

  managed_node_groups = {
    general_purpose = {
      instance_types = [var.eks_node_instance_type] # Dev specific instance type
      min_size       = 1
      max_size       = 2 # Scaled down for dev
      desired_size   = 1
      disk_size      = 20 # Smaller disk for dev
    }
  }

  # Secrets integration (REQ-8-011)
  enable_external_secrets_operator = true
  external_secrets_iam_role_name   = "${local.resource_prefix}-eso-role"

  # APM Configuration (REQ-17-005)
  enable_apm_tooling = true
  # apm_config specific variables...

  # Cluster Alerting Configuration (REQ-17-006, REQ-17-007)
  enable_cluster_alerting_components = true

  tags = local.env_tags

  # Depends on RDS master password secret being available if any EKS add-on needs it directly (unlikely for core EKS)
  # depends_on = [module.secrets_manager]
}

# --- Monitoring & Alerting (Prometheus Stack on EKS - REQ-17-005, REQ-17-006, REQ-17-007) ---
module "prometheus_stack" {
  source = "../../modules/monitoring/prometheus_stack"

  # Depends on EKS cluster being ready
  depends_on = [module.eks_cluster]

  eks_cluster_name         = module.eks_cluster.cluster_name
  eks_oidc_provider_arn    = module.eks_cluster.oidc_provider_arn
  eks_cluster_endpoint     = module.eks_cluster.cluster_endpoint
  eks_cluster_ca_certificate = module.eks_cluster.cluster_certificate_authority_data

  namespace                = "monitoring"
  grafana_admin_password_secret_name = "grafana-admin-password-${var.environment_name}" # Store in Secrets Manager
  grafana_pvc_storage_class = "gp2" # Example, ensure storage class exists or use dynamic
  grafana_pvc_size          = "1Gi" # Small for dev

  prometheus_pvc_storage_class = "gp2"
  prometheus_pvc_size          = "2Gi" # Small for dev

  alertmanager_config_secret_name = "alertmanager-config-${var.environment_name}" # Store in SM or pass directly
  # alertmanager_config should route to SNS topics from module.sns_topics

  alert_rules_enabled = true
  # common_alert_rules specific variables could be passed here

  tags = local.env_tags
}

# --- Alerting SNS Topics (REQ-17-009) ---
module "sns_topics" {
  source = "../../modules/alerting/sns_topics"

  topic_names_with_subscriptions = {
    "critical_alerts" = [{ protocol = "email", endpoint = "dev-alerts-critical@example.com" }],
    "warning_alerts"  = [{ protocol = "email", endpoint = "dev-alerts-warning@example.com" }],
    "security_alerts" = [{ protocol = "email", endpoint = "dev-security-alerts@example.com" }], # REQ-17-008
    "backup_alerts"   = [{ protocol = "email", endpoint = "dev-backup-alerts@example.com" }]      # REQ-17-008
  }
  allowed_publishers = [
    "cloudwatch.amazonaws.com",
    "events.amazonaws.com",
    # Add other service principals if needed
  ]
  tags = local.env_tags
}

# --- Backup (AWS Backup - REQ-17-008 for failure alerts) ---
module "aws_backup" {
  source = "../../modules/backup_restore/aws_backup"

  backup_vault_name = "${local.resource_prefix}-backup-vault"
  backup_plan_name  = "${local.resource_prefix}-daily-backup-plan"
  backup_schedule   = "cron(0 5 * * ? *)" # Daily at 5 AM UTC
  backup_tags_selection = {
    "dev-backup" = "true" # Tag resources with 'dev-backup: true'
  }
  backup_resources_selection = [
    # module.rds_aurora_postgresql.cluster_arn # Example direct resource ARN
  ]
  cold_storage_after_days = 0 # No cold storage for dev, or very short
  delete_after_days       = 30 # Shorter retention for dev backups

  # Alerting for backup failures (REQ-17-008)
  enable_backup_alerts = true
  backup_alerts_sns_topic_arn = module.sns_topics.topic_arns["backup_alerts"]

  tags = local.env_tags
}

# --- Security (GuardDuty - REQ-17-008 for findings) ---
module "guardduty" {
  source = "../../modules/security/guardduty"

  enable_guardduty               = true
  publish_findings_to_s3       = false # Optional for dev
  # s3_bucket_for_findings_arn = module.s3_guardduty_findings.bucket_arn # If s3 publishing enabled
  enable_eventbridge_alerts    = true
  findings_sns_topic_arn       = module.sns_topics.topic_arns["security_alerts"]
  guardduty_finding_severities = ["MEDIUM", "HIGH"] # Monitor medium and high for dev

  tags = local.env_tags
}

# --- AWS Config Rules (Example) ---
module "s3_public_access_blocked_rule" {
  source = "../../policies/aws_config_rules/ensure_s3_public_access_blocked"

  rule_name = "${local.resource_prefix}-s3-public-access-blocked"
  # Other params if needed
}

# --- Data Sources ---
data "aws_availability_zones" "available" {}

# --- Outputs (example) ---
output "dev_vpc_id" {
  description = "ID of the development VPC"
  value       = module.vpc.vpc_id
}

output "dev_eks_cluster_name" {
  description = "Name of the development EKS cluster"
  value       = module.eks_cluster.cluster_name
}

output "dev_eks_cluster_endpoint" {
  description = "Endpoint for the development EKS cluster"
  value       = module.eks_cluster.cluster_endpoint
  sensitive   = true
}

output "dev_rds_cluster_endpoint" {
  description = "Endpoint for the development RDS Aurora cluster"
  value       = module.rds_aurora_postgresql.cluster_endpoint
  sensitive   = true
}

output "dev_critical_alerts_sns_topic_arn" {
  description = "ARN of the critical alerts SNS topic for dev"
  value       = module.sns_topics.topic_arns["critical_alerts"]
}