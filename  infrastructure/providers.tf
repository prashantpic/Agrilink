provider "aws" {
  region = var.aws_region

  default_tags {
    tags = merge(
      var.default_tags,
      {
        "Environment"   = terraform.workspace == "default" ? "dev" : terraform.workspace # Overrides global-root for resources created by this provider instance at root level if any
        "tf_workspace"  = terraform.workspace
        "Orchestration" = "Root"
      }
    )
  }

  # Optionally, specify allowed account IDs for an extra layer of safety.
  # allowed_account_ids = ["123456789012"]

  # Assume role configuration if needed for cross-account access or specific permissions.
  # assume_role {
  #   role_arn = "arn:aws:iam::ACCOUNT_ID:role/TerraformExecutionRole"
  # }
}

# Kubernetes Provider configuration
# This provider instance at the root level might be used for operations
# that span environments or for interacting with a management cluster.
# More commonly, environment-specific K8s clusters will have their providers
# configured within the environment module or use outputs from it.
provider "kubernetes" {
  alias = "management_cluster" # Example alias if there's a central management K8s

  # Configuration for the Kubernetes provider.
  # This might be statically configured if connecting to a fixed management cluster
  # or dynamically configured using data sources or outputs from another module.
  # For example, if the EKS cluster is created by an environment module:
  # config_path    = "~/.kube/config" # Or specific path
  # host                   = module.environment_deployment.eks_cluster_endpoint # If root needs to connect
  # cluster_ca_certificate = base64decode(module.environment_deployment.eks_cluster_ca_certificate) # If root needs to connect
  # token                  = module.environment_deployment.eks_cluster_token # If root needs to connect

  # If not connecting to a specific cluster at the root level immediately,
  # these can be left blank or minimally configured, to be overridden or
  # instantiated with specific configurations by modules that use this alias.
}

# Helm Provider configuration
# Similar to the Kubernetes provider, this configures Helm for root-level operations.
provider "helm" {
  alias = "management_cluster_helm" # Example alias

  kubernetes {
    # This block configures how the Helm provider authenticates to the Kubernetes cluster.
    # It can reference the aliased Kubernetes provider or be configured independently.
    # For example, to use the 'management_cluster' Kubernetes provider defined above:
    # config_path = "~/.kube/config" # Or specific path, matching the k8s provider if needed.
    # host                   = provider.kubernetes.management_cluster.host # If deriving from aliased k8s provider
    # cluster_ca_certificate = provider.kubernetes.management_cluster.cluster_ca_certificate
    # token                  = provider.kubernetes.management_cluster.token

    # If the Helm provider configuration depends on outputs from an environment (e.g., EKS cluster),
    # these attributes would be set dynamically.
    # host                   = module.environment_deployment.eks_cluster_endpoint
    # cluster_ca_certificate = base64decode(module.environment_deployment.eks_cluster_ca_certificate)
    # token                  = module.environment_deployment.eks_cluster_token
  }
}

# Default Kubernetes and Helm providers (without alias)
# These would be used if a resource doesn't specify a provider alias.
# Their configuration would typically come from environment module outputs if used at root.
provider "kubernetes" {
  # host                   = try(module.environment_deployment.eks_cluster_endpoint, null)
  # cluster_ca_certificate = try(base64decode(module.environment_deployment.eks_cluster_ca_certificate), null)
  # token                  = try(module.environment_deployment.eks_cluster_token, null)
  # If not configured, operations requiring this provider at root will fail unless
  # the environment module implicitly configures it for its own scope.
}

provider "helm" {
  kubernetes {
    # host                   = try(module.environment_deployment.eks_cluster_endpoint, null)
    # cluster_ca_certificate = try(base64decode(module.environment_deployment.eks_cluster_ca_certificate), null)
    # token                  = try(module.environment_deployment.eks_cluster_token, null)
  }
}