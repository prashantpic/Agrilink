# environments/dev/terraform.tfvars
# Provide concrete, non-sensitive values for variables in the development environment.
# These values override defaults in variables.tf files (root and environment-specific).

# --- General Settings ---
# aws_region        = "us-east-1" # Assuming this is set in root or has a default in dev/variables.tf
# project_name      = "my-app"    # Assuming this is set in root or has a default
environment_name  = "dev"

default_tags = {
  "Owner"       = "DevTeam",
  "CostCenter"  = "Development",
  "Terraform"   = "true"
}

# --- VPC Specific Values ---
vpc_cidr_block      = "10.1.0.0/16"
public_subnet_cidrs = ["10.1.1.0/24", "10.1.2.0/24"]
private_subnet_cidrs = ["10.1.101.0/24", "10.1.102.0/24"]
# availability_zones will typically be chosen dynamically by AWS provider or module
enable_nat_gateway  = true
single_nat_gateway  = true

# --- EKS Specific Values ---
eks_cluster_version           = "1.28"
eks_node_group_instance_types = ["t3.medium"]
eks_node_group_desired_size   = 2
eks_node_group_min_size       = 1
eks_node_group_max_size       = 3

# --- RDS Specific Values ---
rds_engine_version          = "15.5" # For Aurora PostgreSQL
rds_instance_class          = "db.t3.medium" # Or Aurora compatible e.g. "db.r6g.large" if t3 is not suitable for Aurora
rds_instances_count         = 1
rds_db_name                 = "myapp_dev_db"
rds_master_username         = "devdbadmin"
# rds_master_password is sensitive, set in secrets.tfvars (or injected)
rds_backup_retention_period = 7

# --- Monitoring & Alerting Specific Values ---
# grafana_admin_password is sensitive, set in secrets.tfvars (or injected)
critical_alerts_email = "dev-team-critical@example.com"
warning_alerts_email  = "dev-team-warning@example.com"
backup_alerts_email   = "dev-ops-backup@example.com"
security_alerts_email = "dev-security-team@example.com"

# --- AWS Backup Specific Values ---
# This ARN needs to be valid and pre-exist or created by another TF configuration.
# Replace 123456789012 with your AWS Account ID.
aws_backup_iam_role_arn = "arn:aws:iam::123456789012:role/service-role/AWSBackupDefaultServiceRole"