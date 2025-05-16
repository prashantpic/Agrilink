terraform {
  backend "s3" {
    # This backend configuration will be dynamically populated by CI/CD pipeline
    # or a terraform init command with -backend-config arguments.
    # Example:
    # bucket         = "your-terraform-root-state-bucket-name"
    # key            = "global/root.tfstate"
    # region         = "your-aws-region"
    # dynamodb_table = "your-terraform-root-lock-table"
    # encrypt        = true
  }
}

# This main.tf orchestrates the deployment of specific environments.
# It uses the current Terraform workspace to determine which environment
# configuration to apply from the 'environments/' directory.

# Ensure workspaces like 'dev', 'staging', 'prod' exist and correspond
# to directories in 'environments/'.

module "environment" {
  source = "./environments/${terraform.workspace}"

  # Pass root-level variables to the environment module
  aws_region         = var.aws_region
  project_name       = var.project_name
  organization_name  = var.organization_name
  default_tags       = var.default_tags
  environment_name   = terraform.workspace

  # Add other common variables to be passed to all environments if needed
}