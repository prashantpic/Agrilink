# This file defines provider configurations at the root level.
# The main.tf file instantiates these providers.

# AWS Provider Configuration
# This is the primary provider configuration at the root level.
# Specific configurations like region and default_tags are passed from variables.
# The `provider "aws"` block in `main.tf` handles the actual instantiation with these variables.
# No separate `provider "aws"` block is needed here if `main.tf` already configures it based on root variables.
# However, if specific root-level AWS resources were to be created directly in this root module
# (outside of environment modules), then a provider block here could be explicit.
# For this design, `main.tf` handles the AWS provider instantiation.

# Kubernetes Provider Placeholder
# The generic `provider "kubernetes"` declaration is in `versions.tf`.
# The instantiation with configuration (e.g., EKS cluster endpoint, auth)
# is typically done within each environment's scope after an EKS cluster is created,
# or by passing outputs from the EKS module to the provider configuration in `main.tf`.
# At the root level, we might define an unconfigured provider or an aliased provider
# if there's a common K8s management task from root (rare for this pattern).
# The `provider "kubernetes"` block in `main.tf` serves as the primary declaration spot.

# Helm Provider Placeholder
# Similar to the Kubernetes provider, the generic `provider "helm"` declaration is in `versions.tf`.
# Instantiation and configuration are usually tied to a specific Kubernetes cluster.
# The `provider "helm"` block in `main.tf` serves as the primary declaration spot.

# Note on provider blocks:
# Terraform requires provider blocks to be present if you are using resources from that provider.
# The `versions.tf` declares the `required_providers`.
# The `main.tf` in this design instantiates the AWS, Kubernetes, and Helm providers
# using root variables for AWS and placeholders for Kubernetes/Helm (expecting configuration from environment outputs).
# This `providers.tf` file is more for documentation or if there were a need for provider aliases or
# multiple configurations of the same provider at the root level, which is not the case here.
# For clarity and adherence to the SDS mentioning this file, we'll keep it minimal,
# acknowledging that `main.tf` contains the active provider instantiations.

# If there were specific root-level resources needing provider configuration distinct from main.tf,
# they would be defined here. For example, an aliased AWS provider for a different region:
# provider "aws" {
#   alias  = "management_account_region"
#   region = "us-west-2" # Example
# }

# The SDS states: "Specifies AWS region, allowed account IDs, and default tags for the AWS provider.
# Configures Kubernetes and Helm providers, potentially aliased for different clusters or contexts."
# The actual instantiation for the primary providers based on root variables is in `main.tf`.
# This file could be used for aliased providers or if `main.tf` was only for orchestration.
# Given the current `main.tf` structure, this file becomes largely conceptual for the primary providers.
# Let's assume this file is for defining *additional* provider configurations or aliases if needed.

# Example: If you needed to assume a role for the AWS provider at the root level:
# provider "aws" {
#   region = var.aws_region
#   default_tags {
#     tags = var.default_tags
#   }
#   assume_role {
#     role_arn = "arn:aws:iam::ACCOUNT_ID:role/TerraformExecutionRole"
#   }
# }
# However, the main.tf already configures the default AWS provider.
# This file is often used to configure providers if they are not configured in the root main.tf
# or if multiple configurations (aliases) are needed.

# For the described architecture, `main.tf` effectively serves the role of configuring the primary providers
# using root variables. This file would be more relevant for:
# 1. Provider aliases.
# 2. Default provider configurations if not handled in `main.tf`.
# Since `main.tf` covers the default AWS provider, and K8s/Helm are placeholders there,
# this file can remain empty or contain comments explaining its role.

/*
This file is intended for defining and configuring providers used by Terraform at the root level.
In the current setup:
- The `versions.tf` file specifies the required providers and their versions.
- The `main.tf` file at the root level instantiates the primary `aws`, `kubernetes`, and `helm` providers.
  - The `aws` provider in `main.tf` is configured using root variables (`var.aws_region`, `var.default_tags`).
  - The `kubernetes` and `helm` providers in `main.tf` are placeholders, as their full configuration
    typically depends on outputs from environment-specific EKS cluster deployments.

This `providers.tf` file would be used if:
1.  You need to define provider aliases (e.g., multiple AWS provider configurations for different regions or accounts).
    Example:
    provider "aws" {
      alias  = "secondary_region"
      region = "us-west-2"
    }
2.  You have root-level resources that require a specific provider configuration not covered by `main.tf`.

For the current design focusing on orchestrating environment modules, the provider instantiations in `main.tf`
are sufficient for the primary, non-aliased providers.
*/