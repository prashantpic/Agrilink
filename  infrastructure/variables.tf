variable "aws_region" {
  description = "The AWS region to deploy resources in."
  type        = string
  default     = "us-east-1"
}

variable "aws_profile" {
  description = "AWS CLI profile to use for authentication. Optional, mainly for local development."
  type        = string
  default     = null # AWS SDK will use default credential chain if null
}

variable "project_name" {
  description = "The name of the project."
  type        = string
  default     = "my-app"
}

variable "organization_name" {
  description = "The name of the organization."
  type        = string
  default     = "my-org"
}

variable "default_tags" {
  description = "Default tags to apply to all provisioned resources."
  type        = map(string)
  default = {
    "Terraform"   = "true"
    "Project"     = "my-app"    # Should ideally use var.project_name
    "Environment" = "shared"    # Will be overridden by environment-specific tags
    "ManagedBy"   = "Terraform"
  }
}

variable "deploy_environment" {
  description = "The specific environment to deploy (e.g., dev, staging, prod). Overrides terraform.workspace if set."
  type        = string
  default     = "" # If empty, terraform.workspace will be used in main.tf
  validation {
    condition     = var.deploy_environment == "" || contains(["dev", "staging", "prod"], var.deploy_environment)
    error_message = "Allowed values for deploy_environment are 'dev', 'staging', 'prod', or empty string to use workspace."
  }
}