# environments/dev/backend.tf
# Purpose: Configure remote state storage for the development environment's Terraform state.
# LogicDescription: Defines the S3 bucket, key (e.g., 'env:/dev/terraform.tfstate'), region,
# and DynamoDB table for state locking, specific to the 'dev' environment.
# This ensures state isolation between environments.
# ImplementedFeatures: Dev S3 Backend Configuration, Dev DynamoDB State Lock Configuration

terraform {
  backend "s3" {
    bucket         = "my-app-terraform-state-bucket-dev" # Replace with your actual S3 bucket name for dev state
    key            = "env/dev/terraform.tfstate"
    region         = "us-east-1"                         # Replace with your desired AWS region
    dynamodb_table = "my-app-terraform-state-lock-dev" # Replace with your actual DynamoDB table name for dev
    encrypt        = true
  }
}