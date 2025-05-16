# environments/staging/main.tf
# Purpose: Define and orchestrate all infrastructure resources for the staging environment.
# LogicDescription: Calls shared infrastructure modules with configurations specific to 'staging'.
# Parameters often mirror production but with potentially scaled-down resources or specific testing configurations.
# Integrates with staging-specific secrets and monitoring.

# --- Core Networking ---
module "vpc" {
  source = "../../modules/networking/vpc"

  vpc_cidr_block      = var.vpc_cidr_block       # Staging specific value from staging/variables.tf
  public_subnet_cidrs = var.public_subnet_cidrs  # Staging specific value
  private_subnet_cidrs = var.private_subnet_cidrs # Staging specific value
  availability_zones  = var.availability_zones   # Staging specific or default
  enable_nat_gateway  = var.enable_nat_gateway   # Staging specific (likely true)
  single_nat_gateway  = var.single_nat_gateway   # Staging specific (potentially false for HA testing)
  project_name        = var.project_name
  environment         = var.environment_name
  common_tags         = local.common_tags
}

# --- EKS Cluster ---
module "eks_cluster" {
  source = "../../modules/kubernetes/eks_cluster"

  cluster_name                  = "${var.project_name}-${var.environment_name}-eks"
  cluster_version               = var.eks_cluster_version           # Staging specific
  vpc_id                        = module.vpc.vpc_id
  private_subnet_ids            = module.vpc.private_subnet_ids
  control_plane_subnet_ids      = module.vpc.private_subnet_ids
  node_group_instance_types     = var.eks_node_group_instance_types # Staging specific (e.g., m5.large)
  node_group_desired_size       = var.eks_node_group_desired_size   # Staging specific (e.g., 2-3)
  node_group_min_size           = var.eks_node_group_min_size
  node_group_max_size           = var.eks_node_group_max_size
  project_name                  = var.project_name
  environment                   = var.environment_name
  aws_region                    = var.aws_region
  common_tags                   = local.common_tags
  enable_external_secrets       = true # REQ-8-011
  external_secrets_iam_role_name = "${var.project_name}-${var.environment_name}-eso-role"
  enable_prometheus_stack_dependency_config = true # For APM/Alerting REQ-17-005, REQ-17-006
}

# --- Database (RDS Aurora PostgreSQL) ---
module "rds_aurora_postgresql" {
  source = "../../modules/database/rds_aurora"

  cluster_identifier      = "${var.project_name}-${var.environment_name}-aurora-pg"
  engine                  = "aurora-postgresql"
  engine_version          = var.rds_engine_version            # Staging specific
  instance_class          = var.rds_instance_class            # Staging specific (e.g., db.r6g.large)
  instances_count         = var.rds_instances_count           # Staging specific (e.g., 2 for HA)
  vpc_id                  = module.vpc.vpc_id
  db_subnet_group_name    = module.vpc.database_subnet_group_name
  vpc_security_group_ids  = [module.vpc.default_security_group_id] # Dedicated SG recommended
  db_name                 = var.rds_db_name                   # Staging specific
  master_username         = var.rds_master_username           # Staging specific
  master_password         = var.rds_master_password           # Securely injected for staging
  backup_retention_period = var.rds_backup_retention_period # Staging specific (e.g., 15 days)
  skip_final_snapshot     = false                             # Keep final snapshot for staging
  project_name            = var.project_name
  environment             = var.environment_name
  common_tags             = local.common_tags
  enable_monitoring_alerts = true # REQ-17-007
  alarm_sns_topic_arn      = module.sns_topics.alert_topics["critical_alerts_topic"].arn
}

# --- Secrets Management (AWS Secrets Manager for App Secrets) ---
# REQ-8-011
module "application_secrets" {
  source = "../../modules/security/secrets_manager"

  secrets_config = {
    "myapplication/staging/api_key" = {
      description             = "API Key for My Application in Staging"
      recovery_window_in_days = 7
      secret_string           = "staging-dummy-api-key-to-be-replaced" # Placeholder
      tags                    = local.common_tags
    }
    # Add other staging specific secrets
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
  eks_cluster_ca_certificate = module.eks_cluster.cluster_ca_certificate

  namespace                 = "monitoring"
  create_namespace          = true
  grafana_admin_password    = var.grafana_admin_password # Securely injected for staging
  alertmanager_config       = var.staging_alertmanager_config # Defined in staging/variables.tf or tfvars
                                                            # Similar structure to dev, but pointing to staging SNS topics
  prometheus_rules          = var.staging_prometheus_rules    # Defined in staging/variables.tf or tfvars
  common_tags               = local.common_tags
}

# --- SNS Topics for Alerting ---
# REQ-17-006, REQ-17-009
module "sns_topics" {
  source = "../../modules/alerting/sns_topics"

  topic_names_with_config = {
    "critical_alerts_topic" = {
      display_name = "${var.project_name}-${var.environment_name}-critical-alerts"
      subscriptions = [
        { protocol = "email", endpoint = var.critical_alerts_email } # Staging specific email
      ]
    },
    "warning_alerts_topic" = {
      display_name = "${var.project_name}-${var.environment_name}-warning-alerts"
      subscriptions = [
        { protocol = "email", endpoint = var.warning_alerts_email } # Staging specific email
      ]
    },
    "backup_failure_alerts_topic" = { # REQ-17-008
      display_name = "${var.project_name}-${var.environment_name}-backup-failures"
      subscriptions = [
        { protocol = "email", endpoint = var.backup_alerts_email } # Staging specific email
      ]
    },
    "security_alerts_topic" = { # REQ-17-008
      display_name = "${var.project_name}-${var.environment_name}-security-alerts"
      subscriptions = [
        { protocol = "email", endpoint = var.security_alerts_email } # Staging specific email
      ]
    }
  }
  common_tags = local.common_tags
}

# --- AWS Backup ---
# REQ-17-008
module "aws_backup" {
  source = "../../modules/backup_restore/aws_backup"

  vault_name           = "${var.project_name}-${var.environment_name}-backup-vault"
  backup_plan_name     = "${var.project_name}-${var.environment_name}-daily-backup-plan"
  iam_role_arn         = var.aws_backup_iam_role_arn      # Staging specific role or same as dev
  schedule_expression  = "cron(0 4 ? * * *)"            # Daily at 4 AM UTC for staging
  resource_assignments = var.staging_backup_resource_assignments # Defined in staging/variables.tf or tfvars
  enable_alerting      = true
  failure_sns_topic_arn = module.sns_topics.alert_topics["backup_failure_alerts_topic"].arn
  common_tags          = local.common_tags
}

# --- Security (AWS GuardDuty) ---
# REQ-17-008
module "guardduty" {
  source = "../../modules/security/guardduty"

  enable_guardduty             = true
  finding_publishing_frequency = "ONE_HOUR" # Staging specific
  export_findings_to_s3        = var.guardduty_export_findings_s3_staging # Staging specific bool
  s3_bucket_name               = var.guardduty_s3_bucket_name_staging     # If above is true
  enable_alerting              = true
  alert_sns_topic_arn          = module.sns_topics.alert_topics["security_alerts_topic"].arn
  alert_severity_threshold_gte = 6 # Example: Medium and High for staging
  common_tags                  = local.common_tags
}

# --- Common Local Variables ---
locals {
  common_tags = merge(
    var.default_tags, # Assuming staging/variables.tf defines default_tags or inherits from root
    {
      "Environment" = var.environment_name,
      "Project"     = var.project_name
    }
  )
}

# --- Outputs from the Staging Environment ---
output "vpc_id" {
  description = "The ID of the VPC for staging."
  value       = module.vpc.vpc_id
}

output "eks_cluster_name" {
  description = "The name of the EKS cluster for staging."
  value       = module.eks_cluster.cluster_name
}

output "rds_aurora_cluster_endpoint" {
  description = "The endpoint of the RDS Aurora cluster for staging."
  value       = module.rds_aurora_postgresql.cluster_endpoint
}
# Add other relevant outputs for staging