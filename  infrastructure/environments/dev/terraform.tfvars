# environments/dev/terraform.tfvars
# Purpose: Provide concrete, non-sensitive values for variables in the development environment.
# LogicDescription: Sets values for variables defined in 'environments/dev/variables.tf' and root 'variables.tf'.
# Examples: 'eks_cluster_name = "dev-cluster"', 'rds_instance_class = "db.t3.small"'.
# This file should not contain any secrets.
# ImplementedFeatures: Dev Environment Configuration Values

aws_region     = "us-east-1"
project_name   = "my-app"
organization_name = "MyOrg"
default_tags = {
  "Terraform"   = "true"
  "CostCenter"  = "dev-team"
  "Application" = "my-app"
}

# Values for variables defined in environments/dev/variables.tf
# vpc_cidr             = "10.1.0.0/16" # Default is fine
# public_subnet_cidrs  = ["10.1.1.0/24", "10.1.2.0/24"] # Default is fine
# private_subnet_cidrs = ["10.1.101.0/24", "10.1.102.0/24"] # Default is fine

# eks_cluster_version    = "1.28" # Default is fine
# eks_node_instance_type = "t3.medium" # Default is fine

# rds_engine_version     = "15.5" # Default is fine
# rds_instance_class     = "db.t3.medium" # Default is fine for Aurora Serverless v2 compatible or provisioned

# dev_feature_flag_x     = true # Default is fine
# dev_domain_name        = "dev.myapp.example.com" # Example of overriding default