# This output reflects the environment that was deployed by the root module.
output "deployed_environment" {
  description = "The name of the environment that was deployed."
  value       = module.environment_deployment.environment_name # Assuming environment module outputs its name
}

# Example: Aggregate or pass through key outputs from the deployed environment.
# The actual available outputs will depend on what each environment module exposes.

output "environment_vpc_id" {
  description = "The VPC ID of the deployed environment."
  value       = try(module.environment_deployment.vpc_id, null)
  sensitive   = false # VPC ID is generally not sensitive
}

output "environment_eks_cluster_name" {
  description = "The EKS cluster name of the deployed environment."
  value       = try(module.environment_deployment.eks_cluster_name, null)
  sensitive   = false
}

output "environment_eks_cluster_endpoint" {
  description = "The EKS cluster endpoint of the deployed environment."
  value       = try(module.environment_deployment.eks_cluster_endpoint, null)
  sensitive   = true # Endpoint might be considered sensitive
}

output "environment_rds_cluster_endpoint" {
  description = "The RDS/Aurora cluster endpoint of the deployed environment."
  value       = try(module.environment_deployment.rds_cluster_endpoint, null)
  sensitive   = true
}

# Add other outputs as needed, ensuring they are defined in the environment modules.
# For example:
# output "environment_s3_bucket_app_data_arn" {
#   description = "ARN of the application data S3 bucket in the deployed environment."
#   value       = try(module.environment_deployment.s3_bucket_app_data_arn, null)
# }