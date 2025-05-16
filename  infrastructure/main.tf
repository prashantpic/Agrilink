terraform {
  # Backend configuration for the root module.
  # This state file primarily manages the orchestration of environments.
  # Specific S3 bucket and DynamoDB table should be created beforehand.
  backend "s3" {
    # Variables for bucket, key, region, and dynamodb_table will be typically
    # passed via CLI '-backend-config' arguments or a backend configuration file.
    # Example:
    # bucket         = "my-terraform-root-state-bucket"
    # key            = "global/root/terraform.tfstate"
    # region         = "us-east-1"
    # dynamodb_table = "my-terraform-root-lock-table"
    # encrypt        = true
  }
}

# This local variable helps select the environment configuration to deploy.
# It defaults to the current workspace name.
locals {
  environment = terraform.workspace == "default" ? "dev" : terraform.workspace # Default to 'dev' if workspace is 'default'
  # You can add more sophisticated logic here to map workspaces to environment names if needed.
}

# Module to deploy the selected environment.
# The source path points to the specific environment's configuration directory.
module "environment_deployment" {
  source = "./environments/${local.environment}"

  # Pass common variables from the root configuration to the environment module.
  aws_region        = var.aws_region
  project_name      = var.project_name
  organization_name = var.organization_name
  default_tags      = var.default_tags

  # Add any other common variables that environment modules might expect.
  # For example:
  # global_billing_tag = var.global_billing_tag
}