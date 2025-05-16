# environments/prod/main.tf
# Purpose: Define and orchestrate all infrastructure resources for the production environment.
# LogicDescription: Calls shared infrastructure modules with production-grade configurations
# focusing on high availability, scalability, security, and performance.
# Uses production-specific .tfvars and secrets. All monitoring and alerting are configured for production SLAs.

# --- Core Networking ---
module "vpc" {
  source = "../../modules/networking/vpc"

  vpc_cidr_block      = var.vpc_cidr_block       # Prod specific value from prod/variables.tf
  public_subnet_cidrs = var.public_subnet_cidrs  # Prod specific value (e.g., across 3 AZs)
  private_subnet_cidrs = var.private_subnet_cidrs # Prod specific value (e.g., across 3 AZs)
  availability_zones  = var.availability_zones   # Prod specific or default (e.g., 3 AZs)
  enable_nat_gateway  = var.enable_nat_gateway   # Prod specific (true)
  single_nat_gateway  = false                    # Prod: Use multiple NAT Gateways for HA
  project_name        = var.project_name
  environment         = var.environment_name
  common_tags         = local.common_tags
}

# --- EKS Cluster (HA) ---
module "eks_cluster" {
  source = "../../modules/kubernetes/eks_cluster"

  cluster_name                  = "${var.project_name}-${var.environment_name}-eks"
  cluster_version               = var.eks_cluster_version           # Prod specific
  vpc_id                        = module.vpc.vpc_id
  private_subnet_ids            = module.vpc.private_subnet_ids
  control_plane_subnet_ids      = module.vpc.private_subnet_ids     # Ensure these are spread across AZs
  node_group_instance_types     = var.eks_node_group_instance_types # Prod specific (e.g., m5.xlarge or compute optimized)
  node_group_desired_size       = var.eks_node_group_desired_size   # Prod specific (e.g., 3-5 minimum)
  node_group_min_size           = var.eks_node_group_min_size
  node_group_max_size           = var.eks_node_group_max_size
  # Add specific HA configurations if module supports (e.g. multi-AZ node groups by default)
  project_name                  = var.project_name
  environment                   = var.environment_name
  aws_region                    = var.aws_region
  common_tags                   = local.common_tags
  enable_external_secrets       = true # REQ-8-011
  external_secrets_iam_role_name = "${var.project_name}-${var.environment_name}-eso-role"
  enable_prometheus_stack_dependency_config = true # For APM/Alerting REQ-17-005, REQ-17-006
}

# --- Database (RDS Aurora PostgreSQL - HA, Read Replicas) ---
module "rds_aurora_postgresql" {
  source = "../../modules/database/rds_aurora"

  cluster_identifier      = "${var.project_name}-${var.environment_name}-aurora-pg"
  engine                  = "aurora-postgresql"
  engine_version          = var.rds_engine_version            # Prod specific
  instance_class          = var.rds_instance_class            # Prod specific (e.g., db.r6g.xlarge)
  instances_count         = var.rds_instances_count           # Prod specific (e.g., 2-3 for HA and read replicas)
  multi_az                = true                              # Ensured by Aurora, instance placement across AZs
  vpc_id                  = module.vpc.vpc_id
  db_subnet_group_name    = module.vpc.database_subnet_group_name
  vpc_security_group_ids  = [module.vpc.default_security_group_id] # Dedicated SG highly recommended
  db_name                 = var.rds_db_name                   # Prod specific
  master_username         = var.rds_master_username           # Prod specific
  master_password         = var.rds_master_password           # Securely injected for prod
  backup_retention_period = var.rds_backup_retention_period # Prod specific (e.g., 30-35 days)
  skip_final_snapshot     = false                             # Critical to keep final snapshot for prod
  deletion_protection     = true                              # Enable deletion protection for prod
  project_name            = var.project_name
  environment             = var.environment_name
  common_tags             = local.common_tags
  enable_monitoring_alerts = true # REQ-17-007 (Critical SLAs)
  alarm_sns_topic_arn      = module.sns_topics.alert_topics["critical_alerts_topic"].arn
  # Configure enhanced monitoring, performance insights for prod if module supports
}

# --- Secrets Management (AWS Secrets Manager for App Secrets) ---
# REQ-8-011
module "application_secrets" {
  source = "../../modules/security/secrets_manager"

  secrets_config = {
    "myapplication/prod/api_key" = {
      description             = "API Key for My Application in Production"
      recovery_window_in_days = 30 # Longer recovery for prod
      # secret_string or generate_random_password logic
      tags                    = local.common_tags
    },
    # Add other prod specific secrets with rotation if applicable
  }
  common_tags = local.common_tags
}

# --- Monitoring & Alerting (Prometheus Stack on EKS - Critical SLAs) ---
# REQ-17-005, REQ-17-006, REQ-17-007
module "prometheus_stack" {
  source = "../../modules/monitoring/prometheus_stack"

  eks_cluster_name          = module.eks_cluster.cluster_name
  eks_oidc_provider_arn     = module.eks_cluster.oidc_provider_arn
  eks_cluster_endpoint      = module.eks_cluster.cluster_endpoint
  eks_cluster_ca_certificate = module.eks_cluster.cluster_ca_certificate

  namespace                 = "monitoring"
  create_namespace          = true
  grafana_admin_password    = var.grafana_admin_password # Securely injected for prod
  # Production alertmanager config with robust routing and escalation
  alertmanager_config       = var.prod_alertmanager_config # Defined in prod/variables.tf or tfvars
  # Production prometheus rules with critical SLA focus
  prometheus_rules          = var.prod_prometheus_rules    # Defined in prod/variables.tf or tfvars
  # Consider persistent storage for Prometheus/Grafana in prod
  # prometheus_storage_class = "gp3"
  # grafana_storage_class    = "gp3"
  common_tags               = local.common_tags
}

# --- SNS Topics for Alerting ---
# REQ-17-006, REQ-17-009
module "sns_topics" {
  source = "../../modules/alerting/sns_topics"

  topic_names_with_config = {
    "critical_alerts_topic" = {
      display_name = "${var.project_name}-${var.environment_name}-critical-alerts"
      subscriptions = var.prod_critical_alerts_subscriptions # Prod specific (e.g., PagerDuty, OpsGenie, email)
    },
    "warning_alerts_topic" = {
      display_name = "${var.project_name}-${var.environment_name}-warning-alerts"
      subscriptions = var.prod_warning_alerts_subscriptions # Prod specific
    },
    "backup_failure_alerts_topic" = { # REQ-17-008
      display_name = "${var.project_name}-${var.environment_name}-backup-failures"
      subscriptions = var.prod_backup_alerts_subscriptions
    },
    "security_alerts_topic" = { # REQ-17-008
      display_name = "${var.project_name}-${var.environment_name}-security-alerts"
      subscriptions = var.prod_security_alerts_subscriptions
    }
  }
  common_tags = local.common_tags
}

# --- AWS Backup ---
# REQ-17-008
module "aws_backup" {
  source = "../../modules/backup_restore/aws_backup"

  vault_name           = "${var.project_name}-${var.environment_name}-backup-vault"
  # Consider cross-region replication for prod backup vault if module supports
  backup_plan_name     = "${var.project_name}-${var.environment_name}-prod-backup-plan"
  iam_role_arn         = var.aws_backup_iam_role_arn      # Prod specific role
  schedule_expression  = "cron(0 3 ? * * *)"            # Daily at 3 AM UTC for prod
  resource_assignments = var.prod_backup_resource_assignments # Comprehensive assignments for prod
  enable_alerting      = true
  failure_sns_topic_arn = module.sns_topics.alert_topics["backup_failure_alerts_topic"].arn
  common_tags          = local.common_tags
}

# --- Security (AWS GuardDuty) ---
# REQ-17-008
module "guardduty" {
  source = "../../modules/security/guardduty"

  enable_guardduty             = true
  finding_publishing_frequency = "SIX_HOURS" # Prod specific, or as per security policy
  export_findings_to_s3        = var.guardduty_export_findings_s3_prod # Prod specific bool (likely true)
  s3_bucket_name               = var.guardduty_s3_bucket_name_prod     # If above is true, dedicated bucket
  enable_alerting              = true
  alert_sns_topic_arn          = module.sns_topics.alert_topics["security_alerts_topic"].arn
  alert_severity_threshold_gte = 7 # High severity for prod alerts
  common_tags                  = local.common_tags
}

# --- Common Local Variables ---
locals {
  common_tags = merge(
    var.default_tags, # Assuming prod/variables.tf defines default_tags or inherits from root
    {
      "Environment" = var.environment_name,
      "Project"     = var.project_name
    }
  )
}

# --- Outputs from the Production Environment ---
output "vpc_id" {
  description = "The ID of the VPC for production."
  value       = module.vpc.vpc_id
}

output "eks_cluster_name" {
  description = "The name of the EKS cluster for production."
  value       = module.eks_cluster.cluster_name
}

output "rds_aurora_cluster_endpoint" {
  description = "The endpoint of the RDS Aurora cluster for production."
  value       = module.rds_aurora_postgresql.cluster_endpoint
}
# Add other critical outputs for production