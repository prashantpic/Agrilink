terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0" # Specify a specific major version, allow minor/patch updates
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.20" # Specify a version constraint
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.10" # Specify a version constraint
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5" # For generating random values if needed
    }
    # Add other providers as necessary, e.g., template provider
    # template = {
    #   source  = "hashicorp/template"
    #   version = "~> 2.2"
    # }
  }
}