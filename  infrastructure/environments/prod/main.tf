# environments/prod/main.tf
# Purpose: Define and orchestrate all infrastructure resources for the production environment.
# LogicDescription: Calls shared infrastructure modules with production-grade configurations
# focusing on high availability, scalability, security, and performance. Uses production-specific .tfvars and secrets.
# All monitoring and alerting are configured for production SLAs.
# ImplementedFeatures: Prod VPC Setup, Prod EKS Cluster (HA), Prod RDS Instances (HA, Read Replicas), Prod Monitoring & Alerting Setup (Critical SLAs)
# RequirementIds: REQ-8-011, REQ-17-005, REQ-17-006, REQ-17-007, REQ-17-008, REQ-17-009

# Global/Shared Variables (assumed from root or injected)
variable "aws_region" {}
variable "project_name" {}
variable "organization_name" {}
variable "default_tags" {
  type    = map(string)
  default = {}
}

# Environment specific variables (defined in environments/prod/variables.tf)
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
variable "rds_deletion_protection" {}

variable "environment_name" {
  description = "Name of the environment"
  type        = string
  default     = "prod"
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
  availability_zones  = slice(data.aws_availability_zones.available.names, 0, 3) # Prod should use at least 3 AZs for HA
  enable_nat_gateway  = true
  single_nat_gateway  = false # Redundant NAT Gateways for HA in prod

  tags = local.env_tags
}

# --- Secrets Management (REQ-8-011) ---
module "secrets_manager" {
  source = "../../modules/security/secrets_manager"

  secrets_to_create = {
    rds_master_password = {
      description             = "Master password for RDS instance in ${var.environment_name}"
      recovery_window_in_days = 30 # Standard recovery for prod
      enable_rotation         = true # Enable rotation for RDS password in prod
      rotation_lambda_arn     = "arn:aws:lambda:${var.aws_region}:${data.aws_caller_identity.current.account_id}:function:SecretsManagerRDSMariaDBRotationSingleUser" # Example, adjust for PostgreSQL
      rotation_rules = {
        schedule_expression = "cron(0 4 */7 * ? *)" # Rotate every 7 days
      }
      generate_random_password = {
        length           = 20 # Stronger password for prod
        special_characters = true
      }
    }
    # Add other critical secrets for prod, consider rotation for them as well
  }
  common_tags = local.env_tags
}

# --- Database (RDS Aurora PostgreSQL - HA, Read Replicas) ---
module "rds_aurora_postgresql" {
  source = "../../modules/database/rds_aurora"

  cluster_identifier      = "${local.resource_prefix}-aurora-pg"
  engine                  = "aurora-postgresql"
  engine_version          = var.rds_engine_version
  instance_class          = var.rds_instance_class # Production grade instance class
  instances_count         = var.rds_instances_count # e.g., 2 or 3 for HA in prod
  # enable_read_replica   = true # If module supports separate read replica configuration
  # read_replica_count    = 1 # Example
  publicly_accessible     = false
  vpc_id                  = module.vpc.vpc_id
  subnet_ids              = module.vpc.private_subnet_ids
  db_name                 = "${var.project_name}_prod_db"
  master_username         = "adminuser"
  master_password_secret_arn = module.secrets_manager.secrets["rds_master_password"].arn
  backup_retention_period = var.rds_backup_retention_period # e.g., 35 days for prod
  skip_final_snapshot     = false
  storage_encrypted       = true
  deletion_protection     = var.rds_deletion_protection # Should be true for prod

  # Monitoring & Alerting for RDS (REQ-17-007) - Critical SLAs
  enable_cloudwatch_alarms = true
  alarm_sns_topic_arn      = module.sns_topics.topic_arns["critical_alerts_prod_high_sev"]
  # Additional alarms for prod if needed

  tags = local.env_tags
}

# --- Kubernetes (EKS - HA) ---
module "eks_cluster" {
  source = "../../modules/kubernetes/eks_cluster"

  cluster_name    = "${local.resource_prefix}-eks"
  cluster_version = var.eks_cluster_version
  vpc_id          = module.vpc.vpc_id
  subnet_ids      = module.vpc.private_subnet_ids # Ensure these span multiple AZs for HA

  # EKS control plane logging - enable for prod
  enabled_cluster_log_types = ["api", "audit", "authenticator", "controllerManager", "scheduler"]


  managed_node_groups = {
    critical_workloads_prod = {
      instance_types = [var.eks_node_instance_type] # Production grade instance types
      min_size       = var.eks_node_min_size # e.g., 3
      max_size       = var.eks_node_max_size # e.g., 10
      desired_size   = var.eks_node_desired_size # e.g., 3
      disk_size      = 100
      # Consider Spot instances for cost optimization on non-critical workloads if applicable
    }
    # Add more node groups as needed (e.g., for specific workloads with different instance types/requirements)
  }

  # Secrets integration (REQ-8-011)
  enable_external_secrets_operator = true
  external_secrets_iam_role_name   = "${local.resource_prefix}-eso-role"

  # APM Configuration (REQ-17-005)
  enable_apm_tooling = true
  # apm_config specific to production workloads

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

  namespace                = "monitoring-prod" # Dedicated namespace for prod monitoring
  grafana_admin_password_secret_name = "grafana-admin-password-${var.environment_name}" # Managed in AWS SM
  grafana_pvc_storage_class = "gp3" # Use gp3 for better performance/cost options
  grafana_pvc_size          = "20Gi" # Larger for prod

  prometheus_pvc_storage_class = "gp3"
  prometheus_pvc_size          = "100Gi" # Significantly larger for prod data retention

  alertmanager_config_secret_name = "alertmanager-config-${var.environment_name}"
  # Configure Alertmanager for prod SLAs, potentially with PagerDuty/OpsGenie integrations via SNS

  alert_rules_enabled = true
  # specific_alert_rules_prod could be passed here

  tags = local.env_tags
}

# --- Alerting SNS Topics (REQ-17-009) ---
module "sns_topics" {
  source = "../../modules/alerting/sns_topics"

  topic_names_with_subscriptions = {
    "critical_alerts_prod_high_sev" = [
      { protocol = "email", endpoint = "prod-alerts-critical-high@example.com" },
      # { protocol = "sqs", endpoint = "arn:aws:sqs:..." } # For PagerDuty/OpsGenie integration
    ],
    "warning_alerts_prod"  = [{ protocol = "email", endpoint = "prod-alerts-warning@example.com" }],
    "security_alerts_prod" = [{ protocol = "email", endpoint = "prod-security-alerts@example.com" }], # REQ-17-008
    "backup_alerts_prod"   = [{ protocol = "email", endpoint = "prod-backup-alerts@example.com" }]    # REQ-17-008
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
  backup_plan_name  = "${local.resource_prefix}-critical-backup-plan"
  backup_schedule   = "cron(0 3 * * ? *)" # Daily at 3 AM UTC for prod
  backup_tags_selection = {
    "prod-backup" = "true" # Tag critical prod resources
  }
  cold_storage_after_days = 180 # Example: move to cold storage after 6 months
  delete_after_days       = 365 * 7 # Example: retain for 7 years for compliance

  enable_backup_alerts = true
  backup_alerts_sns_topic_arn = module.sns_topics.topic_arns["backup_alerts_prod"]

  tags = local.env_tags
}

# --- Security (GuardDuty - REQ-17-008 for findings) ---
module "guardduty" {
  source = "../../modules/security/guardduty"

  enable_guardduty               = true
  publish_findings_to_s3       = true
  s3_bucket_for_findings_name  = "${local.resource_prefix}-guardduty-findings" # Ensure S3 bucket is provisioned, possibly by another module
  enable_eventbridge_alerts    = true
  findings_sns_topic_arn       = module.sns_topics.topic_arns["security_alerts_prod"]
  guardduty_finding_severities = ["LOW", "MEDIUM", "HIGH"] # Monitor all severities for prod

  tags = local.env_tags
}

# --- AWS Config Rules ---
module "s3_public_access_blocked_rule_prod" {
  source = "../../policies/aws_config_rules/ensure_s3_public_access_blocked"
  rule_name = "${local.resource_prefix}-s3-public-access-blocked"
  # Potentially enable auto-remediation for prod if requirements allow
}


# --- Data Sources ---
data "aws_availability_zones" "available" {}
data "aws_caller_identity" "current" {}


# --- Outputs ---
output "prod_vpc_id" {
  description = "ID of the production VPC"
  value       = module.vpc.vpc_id
}

output "prod_eks_cluster_name" {
  description = "Name of the production EKS cluster"
  value       = module.eks_cluster.cluster_name
}

output "prod_rds_cluster_endpoint" {
  description = "Endpoint for the production RDS Aurora cluster"
  value       = module.rds_aurora_postgresql.cluster_endpoint
  sensitive   = true
}