# environments/dev/variables.tf
# Purpose: Define input variables specific to the development environment configuration.

variable "aws_region" {
  description = "AWS region for the development environment."
  type        = string
  default     = "us-east-1" # Example default, should align with root or be overridden
}

variable "project_name" {
  description = "Name of the project."
  type        = string
  default     = "my-app" # Example default
}

variable "environment_name" {
  description = "Name of the environment, e.g., 'dev', 'staging', 'prod'."
  type        = string
  default     = "dev"
}

variable "default_tags" {
  description = "Default tags to apply to all resources."
  type        = map(string)
  default     = {}
}

# --- VPC Specific Variables ---
variable "vpc_cidr_block" {
  description = "CIDR block for the VPC in the dev environment."
  type        = string
  default     = "10.1.0.0/16" # Example for dev
}

variable "public_subnet_cidrs" {
  description = "List of CIDR blocks for public subnets in dev."
  type        = list(string)
  default     = ["10.1.1.0/24", "10.1.2.0/24"] # Across 2 AZs for dev
}

variable "private_subnet_cidrs" {
  description = "List of CIDR blocks for private subnets in dev."
  type        = list(string)
  default     = ["10.1.101.0/24", "10.1.102.0/24"] # Across 2 AZs for dev
}

variable "availability_zones" {
  description = "List of Availability Zones to use in dev."
  type        = list(string)
  # Default will be populated by data source in root or VPC module
  # Forcing specific AZs for dev if needed: default = ["us-east-1a", "us-east-1b"]
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway for private subnets in dev. Typically true for dev."
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "Use a single NAT Gateway for all private subnets in dev (cost saving)."
  type        = bool
  default     = true
}

# --- EKS Specific Variables ---
variable "eks_cluster_version" {
  description = "Kubernetes version for the EKS cluster in dev."
  type        = string
  default     = "1.28" # Specify desired version
}

variable "eks_node_group_instance_types" {
  description = "List of instance types for EKS worker nodes in dev."
  type        = list(string)
  default     = ["t3.medium"] # Smaller instances for dev
}

variable "eks_node_group_desired_size" {
  description = "Desired number of worker nodes in the EKS node group for dev."
  type        = number
  default     = 2
}

variable "eks_node_group_min_size" {
  description = "Minimum number of worker nodes in the EKS node group for dev."
  type        = number
  default     = 1
}

variable "eks_node_group_max_size" {
  description = "Maximum number of worker nodes in the EKS node group for dev."
  type        = number
  default     = 3
}

# --- RDS Specific Variables ---
variable "rds_engine_version" {
  description = "Database engine version for RDS/Aurora in dev (e.g., PostgreSQL version)."
  type        = string
  default     = "15.5" # Example for Aurora PostgreSQL
}

variable "rds_instance_class" {
  description = "Instance class for RDS/Aurora database instances in dev."
  type        = string
  default     = "db.t3.medium" # Smaller instance for dev (Aurora compatible might be db.r6g.large etc)
                                # For Aurora Serverless v2, this might be different or not applicable.
                                # This example uses provisioned Aurora.
}

variable "rds_instances_count" {
  description = "Number of DB instances in the Aurora cluster for dev."
  type        = number
  default     = 1 # Single instance for dev to save cost
}

variable "rds_db_name" {
  description = "Initial database name for RDS/Aurora in dev."
  type        = string
  default     = "myapp_dev_db"
}

variable "rds_master_username" {
  description = "Master username for the RDS/Aurora database in dev."
  type        = string
  default     = "dbadmin"
}

variable "rds_master_password" {
  description = "Master password for the RDS/Aurora database in dev. Should be injected securely."
  type        = string
  sensitive   = true
  # No default, must be provided via secrets.tfvars or environment variable
}

variable "rds_backup_retention_period" {
  description = "Backup retention period in days for RDS/Aurora in dev."
  type        = number
  default     = 7 # Shorter retention for dev
}

# --- Monitoring & Alerting Specific Variables ---
variable "grafana_admin_password" {
  description = "Admin password for Grafana in dev. Should be injected securely."
  type        = string
  sensitive   = true
  # No default, must be provided
}

variable "critical_alerts_email" {
  description = "Email address for critical alerts in dev."
  type        = string
  default     = "dev-alerts-critical@example.com"
}

variable "warning_alerts_email" {
  description = "Email address for warning alerts in dev."
  type        = string
  default     = "dev-alerts-warning@example.com"
}

variable "backup_alerts_email" {
  description = "Email address for backup failure alerts in dev."
  type        = string
  default     = "dev-alerts-backup@example.com"
}

variable "security_alerts_email" {
  description = "Email address for security alerts (e.g., GuardDuty) in dev."
  type        = string
  default     = "dev-alerts-security@example.com"
}

# --- AWS Backup Specific Variables ---
variable "aws_backup_iam_role_arn" {
  description = "IAM Role ARN for AWS Backup service in dev. Must be pre-created or created by a separate config."
  type        = string
  # Example: "arn:aws:iam::123456789012:role/service-role/AWSBackupDefaultServiceRole"
  # No default, must be provided if AWS Backup module is used.
}