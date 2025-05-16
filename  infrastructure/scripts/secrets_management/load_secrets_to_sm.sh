#!/bin/bash

# scripts/secrets_management/load_secrets_to_sm.sh
# Purpose: Automate the process of populating AWS Secrets Manager with initial or updated secret values.
# REQ-8-011

set -e # Exit immediately if a command exits with a non-zero status.
# set -u # Treat unset variables as an error when substituting.
# set -o pipefail # Causes a pipeline to return the exit status of the last command in the pipe that returned a non-zero return value.

# --- Configuration ---
# AWS Region can be passed as an argument or set as an environment variable
AWS_REGION="${1:-us-east-1}" # Default to us-east-1 if not provided

# --- Helper Functions ---
log_info() {
  echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
  echo "[ERROR] $(date '+%Y-%m-%d %H:%M:%S') - $1" >&2
}

usage() {
  echo "Usage: $0 [aws_region]"
  echo "This script loads secrets into AWS Secrets Manager."
  echo "It expects secret names and values to be passed as environment variables:"
  echo "  SM_SECRET_NAME_<SUFFIX>=<secret_name>"
  echo "  SM_SECRET_VALUE_<SUFFIX>=<secret_value_or_json_string>"
  echo "  SM_SECRET_DESCRIPTION_<SUFFIX>=<description (optional)>"
  echo "Example:"
  echo "  export SM_SECRET_NAME_DBPASS=\"myapplication/dev/db_password\""
  echo "  export SM_SECRET_VALUE_DBPASS='{\"username\":\"admin\",\"password\":\"supersecret\"}'"
  echo "  export SM_SECRET_DESCRIPTION_DBPASS=\"Database credentials for dev\""
  echo "  $0 us-west-2"
  exit 1
}

# Function to check if a secret exists
secret_exists() {
  local secret_name="$1"
  aws secretsmanager describe-secret --secret-id "$secret_name" --region "$AWS_REGION" >/dev/null 2>&1
}

# Function to create or update a secret
# Accepts: secret_name, secret_value (string or JSON string), description (optional)
create_or_update_secret() {
  local secret_name="$1"
  local secret_value="$2"
  local description="${3:-Secret created by load_secrets_to_sm.sh}" # Default description

  if [ -z "$secret_name" ] || [ -z "$secret_value" ]; then
    log_error "Secret name and value must be provided for create_or_update_secret."
    return 1
  fi

  log_info "Processing secret: $secret_name"

  if secret_exists "$secret_name"; then
    log_info "Secret '$secret_name' already exists. Updating secret value."
    if aws secretsmanager update-secret \
      --secret-id "$secret_name" \
      --secret-string "$secret_value" \
      --description "$description" \
      --region "$AWS_REGION"; then
      log_info "Successfully updated secret '$secret_name'."
    else
      log_error "Failed to update secret '$secret_name'."
      return 1
    fi
  else
    log_info "Secret '$secret_name' does not exist. Creating new secret."
    if aws secretsmanager create-secret \
      --name "$secret_name" \
      --secret-string "$secret_value" \
      --description "$description" \
      --region "$AWS_REGION"; then
      log_info "Successfully created secret '$secret_name'."
    else
      log_error "Failed to create secret '$secret_name'."
      return 1
    fi
  fi
}

# --- Main Logic ---
main() {
  log_info "Starting script to load secrets to AWS Secrets Manager in region $AWS_REGION."

  # Check for AWS CLI
  if ! command -v aws &> /dev/null; then
    log_error "AWS CLI could not be found. Please install and configure it."
    exit 1
  fi

  # Check AWS credentials
  if ! aws sts get-caller-identity --region "$AWS_REGION" > /dev/null; then
      log_error "AWS credentials are not configured or invalid for region $AWS_REGION. Please configure AWS CLI."
      exit 1
  fi

  local secrets_found=0
  # Iterate over environment variables looking for SM_SECRET_NAME_*
  for var_name in $(env | grep '^SM_SECRET_NAME_' | cut -d= -f1); do
    local suffix=$(echo "$var_name" | sed 's/SM_SECRET_NAME_//')
    local secret_name_val="${!var_name}"
    
    local value_var_name="SM_SECRET_VALUE_$suffix"
    local secret_value_val="${!value_var_name}"

    local desc_var_name="SM_SECRET_DESCRIPTION_$suffix"
    local secret_description_val="${!desc_var_name}" # This might be empty

    if [ -z "$secret_name_val" ]; then
      log_error "Variable $var_name is set, but its value (secret name) is empty. Skipping."
      continue
    fi

    if [ -z "$secret_value_val" ]; then
      log_error "Secret name $secret_name_val found (from $var_name), but corresponding value variable $value_var_name is not set or empty. Skipping."
      continue
    fi
    
    secrets_found=$((secrets_found + 1))
    create_or_update_secret "$secret_name_val" "$secret_value_val" "$secret_description_val"
  done

  if [ "$secrets_found" -eq 0 ]; then
    log_info "No secrets found to process (no SM_SECRET_NAME_* environment variables set)."
    log_info "Please set environment variables like SM_SECRET_NAME_MYSFX='my/secret' and SM_SECRET_VALUE_MYSFX='myvalue'."
  else
    log_info "Processed $secrets_found secret(s)."
  fi

  log_info "Script finished."
}

# --- Script Execution ---
# Call main function, passing all script arguments
main "$@"