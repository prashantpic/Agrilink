terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0" # Specify a recent, stable version
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.20" # Specify a compatible version
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.10" # Specify a compatible version
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
    # Add other providers if needed, e.g., time, null, template
    # template = {
    #   source  = "hashicorp/template"
    #   version = "~> 2.2"
    # }
  }
}