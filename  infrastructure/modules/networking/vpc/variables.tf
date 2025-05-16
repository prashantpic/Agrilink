# Purpose: Define configurable input parameters for the VPC module.
# LogicDescription: Declares variables for VPC CIDR block, public subnet CIDRs,
# private subnet CIDRs, availability zones, number of NAT gateways (single or per AZ),
# enable/disable DNS hostnames/support, and common tags.

variable "project_name" {
  description = "The name of the project."
  type        = string
}

variable "environment" {
  description = "The environment name (e.g., dev, staging, prod)."
  type        = string
}

variable "aws_region" {
  description = "AWS region where the VPC will be created."
  type        = string
}

variable "vpc_cidr_block" {
  description = "The CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "A list of CIDR blocks for public subnets."
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

variable "private_subnet_cidrs" {
  description = "A list of CIDR blocks for private subnets."
  type        = list(string)
  default     = ["10.0.101.0/24", "10.0.102.0/24", "10.0.103.0/24"]
}

variable "availability_zones" {
  description = "A list of Availability Zones to deploy subnets into."
  type        = list(string)
  # Default AZs should be configured at the environment level or determined dynamically
}

variable "enable_dns_support" {
  description = "A boolean flag to enable/disable DNS support in the VPC."
  type        = bool
  default     = true
}

variable "enable_dns_hostnames" {
  description = "A boolean flag to enable/disable DNS hostnames in the VPC."
  type        = bool
  default     = true
}

variable "enable_nat_gateway" {
  description = "A boolean flag to enable/disable NAT Gateways for private subnets."
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "A boolean flag to provision a single NAT Gateway. If false, one NAT Gateway per AZ will be provisioned."
  type        = bool
  default     = false # Typically false for HA
}

variable "tags" {
  description = "A map of tags to assign to all resources."
  type        = map(string)
  default     = {}
}

variable "dhcp_options_domain_name" {
  description = "Specifies a domain name for the DHCP options set."
  type        = string
  default     = "" # Defaults to region.compute.internal or similar
}

variable "dhcp_options_domain_name_servers" {
  description = "List of DNS servers for the DHCP options set."
  type        = list(string)
  default     = null # Defaults to ["AmazonProvidedDNS"]
}