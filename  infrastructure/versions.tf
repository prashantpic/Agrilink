terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0" # Specify a compatible version range, e.g., 5.x
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.20" # Specify a compatible version range
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.10" # Specify a compatible version range
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5" # Often useful for generating unique names or passwords
    }
    # Add other providers if necessary, e.g., local, template
    # template = {
    #   source  = "hashicorp/template"
    #   version = "~> 2.2"
    # }
  }
}