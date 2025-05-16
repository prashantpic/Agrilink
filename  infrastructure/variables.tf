variable "aws_region" {
  description = "AWS region where the infrastructure will be deployed."
  type        = string
  default     = "us-east-1" # Example default, can be overridden
}

variable "project_name" {
  description = "The name of the project, used for naming resources and tagging."
  type        = string
  default     = "my-app" # Example default
}

variable "organization_name" {
  description = "The name of the organization, used for naming resources and tagging."
  type        = string
  default     = "my-org" # Example default
}

variable "default_tags" {
  description = "A map of default tags to apply to all taggable resources."
  type        = map(string)
  default = {
    "TerraformManaged" = "true"
    "Project"          = "my-app"    # Should ideally use var.project_name
    "Organization"     = "my-org"    # Should ideally use var.organization_name
    "Environment"      = "global-root" # This is for root, environment modules will override
  }
  # Note: To use var.project_name and var.organization_name directly in default,
  # they need to be set before this variable is evaluated, or use locals for dynamic default tags.
  # For simplicity here, they are hardcoded examples but should be dynamic in a real setup.
}

# Example of a global variable that might be passed down
# variable "global_billing_tag" {
#   description = "Global billing tag for cost allocation."
#   type        = string
#   default     = "cost-center-123"
# }