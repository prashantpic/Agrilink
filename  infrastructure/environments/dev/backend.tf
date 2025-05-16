# environments/dev/backend.tf
# Configure remote state storage for the development environment's Terraform state.
# This ensures state isolation between environments.

terraform {
  backend "s3" {
    bucket         = "my-app-terraform-state-bucket-dev" # REPLACE with your actual S3 bucket name for dev state
    key            = "env/dev/terraform.tfstate"
    region         = "us-east-1"                         # REPLACE with your S3 bucket's region
    dynamodb_table = "my-app-terraform-state-lock-dev" # REPLACE with your DynamoDB table name for state locking
    encrypt        = true
  }
}