# Root outputs aggregate key information from the deployed environment.
# The actual values will depend on the outputs defined within each environment module.

output "environment_name" {
  description = "The name of the deployed environment."
  value       = module.environment.environment_name
}

output "vpc_id" {
  description = "The ID of the VPC deployed in the environment."
  value       = module.environment.vpc_id
  sensitive   = false # VPC ID is generally not sensitive
}

output "eks_cluster_name" {
  description = "The name of the EKS cluster deployed in the environment."
  value       = module.environment.eks_cluster_name
  sensitive   = false
}

output "eks_cluster_endpoint" {
  description = "The endpoint for the EKS cluster deployed in the environment."
  value       = module.environment.eks_cluster_endpoint
  sensitive   = true # Endpoint can be considered sensitive
}

output "rds_cluster_endpoint" {
  description = "The endpoint for the RDS/Aurora cluster in the environment."
  value       = module.environment.rds_cluster_endpoint
  sensitive   = true
}

output "rds_cluster_reader_endpoint" {
  description = "The reader endpoint for the RDS/Aurora cluster in the environment."
  value       = module.environment.rds_cluster_reader_endpoint
  sensitive   = true
}

# Add other relevant outputs that might be needed globally,
# ensuring they are exposed by the environment module.
# Example:
# output "application_load_balancer_dns_name" {
#   description = "DNS name of the application load balancer."
#   value       = module.environment.alb_dns_name
# }