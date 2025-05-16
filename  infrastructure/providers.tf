provider "aws" {
  region = var.aws_region

  default_tags {
    tags = merge(
      var.default_tags,
      {
        "Project"     = var.project_name
        "Organization" = var.organization_name
        # Environment tag will be set at the environment module level
      }
    )
  }

  # allowed_account_ids = ["YOUR_AWS_ACCOUNT_ID"] # Optional: restrict to specific account IDs
}

# Kubernetes provider configuration at the root level.
# This might be configured for a central management cluster or left
# generic if each environment configures its own K8s provider instance
# based on its EKS cluster output.
provider "kubernetes" {
  # Configuration for the Kubernetes provider.
  # This typically requires:
  # - host                   = module.eks.cluster_endpoint
  # - cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
  # - token                  = data.aws_eks_cluster_auth.eks.token
  #
  # At the root level, this might be aliased or configured if there's a global
  # Kubernetes context. Otherwise, individual environment modules will configure
  # their specific Kubernetes provider instances using outputs from their EKS cluster.
  # For example, an environment module might do:
  # provider "kubernetes" {
  #   alias                  = "eks_cluster"
  #   host                   = module.eks_cluster.cluster_endpoint
  #   cluster_ca_certificate = base64decode(module.eks_cluster.cluster_certificate_authority_data)
  #   exec {
  #     api_version = "client.authentication.k8s.io/v1beta1"
  #     command     = "aws"
  #     args        = ["eks", "get-token", "--cluster-name", module.eks_cluster.cluster_name, "--region", var.aws_region]
  #   }
  # }
  #
  # If not configured here, Terraform will prompt for configuration or expect
  # environment variables (e.g., KUBECONFIG).
}

# Helm provider configuration at the root level.
# Similar to the Kubernetes provider, this might be generic or configured
# for a central Helm setup. Environment modules typically configure their own
# Helm provider instance linked to their specific EKS cluster.
provider "helm" {
  # Configuration for the Helm provider.
  # This often mirrors the Kubernetes provider configuration.
  # kubernetes {
  #   host                   = module.eks.cluster_endpoint
  #   cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
  #   token                  = data.aws_eks_cluster_auth.eks.token
  #   # Or using exec block similar to kubernetes provider
  #   exec {
  #     api_version = "client.authentication.k8s.io/v1beta1"
  #     command     = "aws"
  #     args        = ["eks", "get-token", "--cluster-name", module.eks_cluster.cluster_name, "--region", var.aws_region]
  #   }
  # }
  #
  # If relying on environment modules for specific Helm deployments into their clusters,
  # they would define their own Helm provider configurations.
}

provider "random" {
  # No specific configuration needed for the random provider.
}