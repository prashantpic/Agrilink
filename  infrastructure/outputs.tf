# Root outputs typically aggregate or expose key information from the deployed environment.
# The specific outputs available will depend on what the environment module (`module.selected_environment`) exports.

output "deployed_environment_name" {
  description = "The name of the environment that was deployed."
  value       = local.environment_name
}

output "environment_vpc_id" {
  description = "The VPC ID of the deployed environment."
  value       = module.selected_environment.vpc_id
  # Sensitive attribute, consider if this should be an output at the root level.
  # This assumes the environment module outputs 'vpc_id'.
}

output "environment_eks_cluster_name" {
  description = "The EKS cluster name of the deployed environment."
  value       = module.selected_environment.eks_cluster_name
  # This assumes the environment module outputs 'eks_cluster_name'.
}

output "environment_eks_cluster_endpoint" {
  description = "The EKS cluster endpoint of the deployed environment."
  value       = module.selected_environment.eks_cluster_endpoint
  # Sensitive attribute, consider if this should be an output at the root level.
  # This assumes the environment module outputs 'eks_cluster_endpoint'.
}

output "environment_rds_cluster_endpoint" {
  description = "The RDS/Aurora cluster endpoint of the deployed environment."
  value       = module.selected_environment.rds_cluster_endpoint
  # Sensitive attribute, consider if this should be an output at the root level.
  # This assumes the environment module outputs 'rds_cluster_endpoint'.
}

# Add other outputs as needed, ensuring they are exposed by the environment modules.
# Example:
# output "environment_public_subnet_ids" {
#   description = "List of public subnet IDs in the deployed environment."
#   value       = module.selected_environment.public_subnet_ids
# }
#
# output "environment_private_subnet_ids" {
#   description = "List of private subnet IDs in the deployed environment."
#   value       = module.selected_environment.private_subnet_ids
# }