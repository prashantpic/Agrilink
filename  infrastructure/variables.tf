variable "aws_region" {
  description = "The AWS region where resources will be deployed."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "The name of the project, used for tagging and naming resources."
  type        = string
  default     = "my-app"
}

variable "organization_name" {
  description = "The name of the organization, used for tagging."
  type        = string
  default     = "my-org"
}

variable "default_tags" {
  description = "Default tags to be applied to all provisioned resources."
  type        = map(string)
  default = {
    "ManagedBy"   = "Terraform"
    "Project"     = "my-app" # This will be overridden by var.project_name in practice
    "Environment" = "global" # This will be overridden by terraform.workspace at env level
  }
}