#!/bin/bash

# scripts/secrets_management/load_secrets_to_sm.sh
# Purpose: Automate the process of populating AWS Secrets Manager with initial or updated secret values.
# LogicDescription: Uses AWS CLI (aws secretsmanager create-secret or aws secretsmanager update-secret commands)
# to load secrets. Secrets can be sourced from environment variables, a local secured file, or prompted.
# Includes error handling and idempotency considerations.
# ImplementedFeatures: Secret Creation in AWS SM, Secret Update in AWS SM
# RequirementIds: REQ-8-011

set -e # Exit immediately if a command exits with a non-zero status.
set -u # Treat unset variables as an error when substituting.
set -o pipefail # Return value of a pipeline is the value of the last command to exit with a non-zero status

# --- Configuration ---
AWS_REGION="${AWS_REGION:-us-east-1}" # Default AWS region if not set externally

# --- Helper Functions ---
log_info() {
    echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo "[ERROR] $(date '+%Y-%m-%d %H:%M:%S') - $1" >&2
}

# Function to create or update a secret
# Arguments:
#   $1: Secret Name (e.g., myapp/dev/db_password)
#   $2: Secret Value (string)
#   $3: Description (optional)
upsert_secret() {
    local secret_name="$1"
    local secret_value="$2"
    local description="${3:-Secret created/updated by load_secrets_to_sm.sh}"

    if [ -z "$secret_name" ] || [ -z "$secret_value" ]; then
        log_error "Secret name and value are required for upsert_secret."
        return 1
    fi

    log_info "Attempting to upsert secret: $secret_name"

    # Check if secret exists
    if aws secretsmanager describe-secret --secret-id "$secret_name" --region "$AWS_REGION" > /dev/null 2>&1; then
        # Secret exists, update it
        log_info "Secret $secret_name already exists. Updating secret value."
        if aws secretsmanager update-secret --secret-id "$secret_name" --secret-string "$secret_value" --description "$description" --region "$AWS_REGION"; then
            log_info "Successfully updated secret: $secret_name"
        else
            log_error "Failed to update secret: $secret_name"
            return 1
        fi
    else
        # Secret does not exist, create it
        log_info "Secret $secret_name does not exist. Creating new secret."
        if aws secretsmanager create-secret --name "$secret_name" --secret-string "$secret_value" --description "$description" --region "$AWS_REGION"; then
            log_info "Successfully created secret: $secret_name"
        else
            log_error "Failed to create secret: $secret_name"
            return 1
        fi
    fi
    return 0
}

# --- Main Script Logic ---
main() {
    log_info "Starting secret loading process..."

    # Example: Load a secret from an environment variable
    # Ensure DB_PASSWORD_DEV environment variable is set securely in your CI/CD or local environment
    # if [ -n "${DB_PASSWORD_DEV:-}" ]; then
    #     upsert_secret "myapp/dev/database_password" "$DB_PASSWORD_DEV" "Development Database Password"
    # else
    #     log_info "DB_PASSWORD_DEV environment variable not set. Skipping."
    # fi

    # Example: Load a secret from a file (use with extreme caution, ensure file is secured and not committed)
    # local secret_file_path="/path/to/secure/dev_api_key.txt"
    # if [ -f "$secret_file_path" ]; then
    #     local api_key_value
    #     api_key_value=$(cat "$secret_file_path")
    #     if [ -n "$api_key_value" ]; then
    #         upsert_secret "myapp/dev/external_api_key" "$api_key_value" "Development External API Key"
    #     else
    #         log_error "Secret file $secret_file_path is empty."
    #     fi
    # else
    #     log_info "Secret file $secret_file_path not found. Skipping."
    # fi

    # Example: Load a static secret directly (less secure, prefer environment variables or files for sensitive data)
    # upsert_secret "myapp/dev/static_config_value" "some_static_value_for_dev" "Static configuration parameter for dev"

    # --- Add more secret loading logic here as needed ---
    # You can make this script more sophisticated by:
    # 1. Accepting parameters for secret name, value, and source.
    # 2. Reading a manifest file (e.g., JSON or YAML) that lists secrets to load.
    # 3. Integrating with other secret providers if necessary before pushing to AWS SM.

    # Example of loading multiple secrets based on command-line arguments:
    # Usage: ./load_secrets_to_sm.sh <secret_name1> <secret_value1> [<secret_name2> <secret_value2> ...]
    # if [ "$#" -eq 0 ] || [ $(($# % 2)) -ne 0 ]; then
    #    log_info "No secrets provided as arguments or incorrect number of arguments."
    #    log_info "Usage: $0 <secret_name1> <secret_value1> [<secret_name2> <secret_value2> ...]"
    # else
    #    while [ "$#" -gt 0 ]; do
    #        local arg_secret_name="$1"
    #        local arg_secret_value="$2"
    #        shift 2 # Shift arguments by 2
    #
    #        log_info "Processing argument secret: $arg_secret_name"
    #        upsert_secret "$arg_secret_name" "$arg_secret_value" "Secret loaded via script arguments"
    #    done
    # fi

    # Placeholder for actual secrets - replace with your logic
    log_info "Simulating loading secrets. Please implement actual secret sourcing."
    if ! upsert_secret "example/dev/app_secret_key" "ThisIsASampleDevSecretValue123!" "Example application secret key for dev"; then
      log_error "Failed to load example/dev/app_secret_key"
    fi

    if ! upsert_secret "example/dev/third_party_api_token" "SampleTokenForThirdPartyDevServiceXYZ" "Example third-party API token for dev"; then
      log_error "Failed to load example/dev/third_party_api_token"
    fi


    log_info "Secret loading process finished."
}

# --- Execute Main ---
# Check if AWS CLI is installed and configured
if ! command -v aws &> /dev/null; then
    log_error "AWS CLI could not be found. Please install and configure it."
    exit 1
fi

# Check AWS CLI credentials
if ! aws sts get-caller-identity --region "$AWS_REGION" > /dev/null 2>&1; then
    log_error "AWS CLI is not configured correctly or credentials are not valid. Please check your AWS configuration."
    exit 1
fi

main "$@"