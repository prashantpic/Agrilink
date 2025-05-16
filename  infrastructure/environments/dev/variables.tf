# environments/dev/variables.tf
# Purpose: Define input variables specific to the development environment configuration.
# LogicDescription: Declares variables like instance sizes (e.g., 't3.small' for dev),
# replica counts for services, specific feature flags for 'dev', domain names (e.g., 'dev.example.com'),
# and any other parameters tailored for the development environment.
# ImplementedFeatures: Dev Environment Variable Definition

variable "vpc_cidr" {
  description = "CIDR block for the VPC in the dev environment."
  type        = string
  default     = "10.1.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "List of CIDR blocks for public subnets in the dev environment."
  type        = list(string)
  default     = ["10.1.1.0/24", "10.1.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "List of CIDR blocks for private subnets in the dev environment."
  type        = list(string)
  default     = ["10.1.101.0/24", "10.1.102.0/24"]
}

variable "eks_cluster_version" {
  description = "Kubernetes version for the EKS cluster in dev."
  type        = string
  default     = "1.28" # Specify a recent, supported version
}

variable "eks_node_instance_type" {
  description = "EC2 instance type for EKS worker nodes in dev."
  type        = string
  default     = "t3.medium" # Smaller instance type for dev
}

variable "rds_engine_version" {
  description = "PostgreSQL engine version for RDS Aurora in dev."
  type        = string
  default     = "15.5" # Specify a specific Aurora PostgreSQL compatible version
}

variable "rds_instance_class" {
  description = "Instance class for RDS Aurora instances in dev."
  type        = string
  default     = "db.t3.medium" # Smaller instance class for dev (adjust based on Aurora compatibility)
}

variable "dev_feature_flag_x" {
  description = "Example feature flag for a dev-specific feature."
  type        = bool
  default     = true
}

variable "dev_domain_name" {
  description = "Domain name for dev environment services."
  type        = string
  default     = "dev.example.com"
}