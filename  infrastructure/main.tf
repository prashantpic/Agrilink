terraform {
  # Backend configuration for the root module.
  # This backend would typically manage the state of the environments themselves,
  # or shared resources not tied to a specific environment.
  # Individual environments will have their own S3 backends.
  # This root backend configuration needs to be bootstrapped (S3 bucket, DynamoDB table).
  backend "s3" {
    # S3 bucket and DynamoDB table for root state should be created manually or via a separate bootstrap script.
    # Example values, replace with actual bootstrapped resource names.
    # bucket         = "my-iac-root-tfstate-bucket"
    # key            = "root/terraform.tfstate"
    # region         = "us-east-1" # Should match var.aws_region
    # dynamodb_table = "my-iac-root-tfstate-lock"
    # encrypt        = true
  }
}

provider "aws" {
  region  = var.aws_region
  profile = var.aws_profile # Optional: for local development
  default_tags {
    tags = var.default_tags
  }
}

# Placeholder for Kubernetes and Helm providers.
# Actual configuration often depends on EKS cluster outputs from environment modules.
# These can be configured with aliases within environment modules or passed EKS outputs.
provider "kubernetes" {
  # Configuration will typically be derived from EKS module outputs in environment configurations.
  # Example:
  # host                   = module.selected_environment.eks_cluster_endpoint
  # cluster_ca_certificate = base64decode(module.selected_environment.eks_cluster_ca_certificate)
  # token                  = module.selected_environment.eks_cluster_auth_token # Or other auth methods
  # An alias might be used if managing multiple clusters from root, though less common for this setup.
}

provider "helm" {
  kubernetes {
    # Configuration will typically be derived from EKS module outputs in environment configurations.
    # Example:
    # host                   = module.selected_environment.eks_cluster_endpoint
    # cluster_ca_certificate = base64decode(module.selected_environment.eks_cluster_ca_certificate)
    # token                  = module.selected_environment.eks_cluster_auth_token # Or other auth methods
  }
}

locals {
  # Use terraform.workspace to select environment if workspaces are used for dev/staging/prod
  # Alternatively, use an input variable like 'var.environment_name'
  environment_name = var.deploy_environment != "" ? var.deploy_environment : terraform.workspace
  # Add validation for environment_name if desired
}

# Dynamically call the selected environment's main configuration.
# This assumes each environment (e.g., dev, staging, prod) has its own `main.tf`
# under `environments/{env}/` which is structured as a module.
module "selected_environment" {
  source = "./environments/${local.environment_name}"

  # Pass root-level variables to the environment module
  aws_region       = var.aws_region
  project_name     = var.project_name
  organization_name = var.organization_name
  default_tags     = var.default_tags

  # Pass any other necessary variables specific to this environment orchestration
  # For example, if there are specific configurations or tfvars files per environment,
  # they will be handled within the environment module itself.
  # Environment-specific tfvars files are typically loaded automatically by Terraform
  # when running `apply` within that environment's directory, or by passing -var-file.
  # Here, we are calling the environment as a module, so its variables need to be passed explicitly
  # or it should source its own tfvars internally (less common for module calls).
  # The current SDS implies environment-specific tfvars are used by the environment's main.tf.
  # For simplicity, direct variable passing is shown. More complex setups might involve
  # loading environment-specific tfvars data sources or other mechanisms.
}