# Description: Handles Kubernetes secrets integration with AWS Secrets Manager via ExternalSecrets operator.
# Purpose: Securely manage and inject secrets from AWS Secrets Manager into Kubernetes pods within the EKS cluster.
# Requirements: REQ-8-011

variable "eks_cluster_name" {
  description = "Name of the EKS cluster."
  type        = string
}

variable "eks_oidc_provider_arn" {
  description = "ARN of the EKS OIDC provider."
  type        = string
}

variable "eks_oidc_provider_url" {
  description = "URL of the EKS OIDC provider (without https://)."
  type        = string
}

variable "aws_region" {
  description = "AWS region for the resources."
  type        = string
}

variable "tags" {
  description = "A map of tags to assign to the resources."
  type        = map(string)
  default     = {}
}

variable "k8s_namespace_external_secrets" {
  description = "Namespace to deploy ExternalSecrets Operator and related resources."
  type        = string
  default     = "external-secrets"
}

variable "external_secrets_helm_chart_version" {
  description = "Version of the ExternalSecrets Helm chart."
  type        = string
  default     = "0.9.11" # Check for the latest stable version
}

variable "external_secrets_iam_role_name" {
  description = "Name for the IAM role for ExternalSecrets service account. If empty, a default will be generated."
  type        = string
  default     = ""
}

variable "allowed_secrets_manager_arns" {
  description = "List of AWS Secrets Manager secret ARNs or ARN patterns that ExternalSecrets can access."
  type        = list(string)
  default     = [] # Example: ["arn:aws:secretsmanager:REGION:ACCOUNT_ID:secret:my-app/*"]
}

resource "kubernetes_namespace" "external_secrets" {
  metadata {
    name = var.k8s_namespace_external_secrets
    labels = {
      name = var.k8s_namespace_external_secrets
    }
  }
}

# IAM Role for ExternalSecrets Operator Service Account (IRSA)
data "aws_iam_policy_document" "external_secrets_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"
    principals {
      type        = "Federated"
      identifiers = [var.eks_oidc_provider_arn]
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider_url}:sub"
      values   = ["system:serviceaccount:${var.k8s_namespace_external_secrets}:external-secrets"] # Default SA name for ESO
    }
    condition {
      test     = "StringEquals"
      variable = "${var.eks_oidc_provider_url}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "external_secrets_operator" {
  name_prefix        = var.external_secrets_iam_role_name == "" ? "ESO-${var.eks_cluster_name}-" : var.external_secrets_iam_role_name
  assume_role_policy = data.aws_iam_policy_document.external_secrets_assume_role_policy.json
  tags               = merge(var.tags, { Name = var.external_secrets_iam_role_name == "" ? "ESO-Role-${var.eks_cluster_name}" : var.external_secrets_iam_role_name })
}

data "aws_iam_policy_document" "external_secrets_access_policy" {
  statement {
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecrets" # Potentially narrow down if using specific secret ARNs
    ]
    resources = length(var.allowed_secrets_manager_arns) > 0 ? var.allowed_secrets_manager_arns : ["*"] # Restrict this in production
    effect    = "Allow"
  }
  # Add KMS permissions if secrets are encrypted with customer-managed KMS keys
  # statement {
  #   actions = [
  #     "kms:Decrypt"
  #   ]
  #   resources = [ "arn:aws:kms:REGION:ACCOUNT_ID:key/YOUR_KMS_KEY_ID" ] # List of KMS key ARNs
  #   effect    = "Allow"
  # }
}

resource "aws_iam_policy" "external_secrets_operator_policy" {
  name_prefix = "ESO-Policy-${var.eks_cluster_name}-"
  policy      = data.aws_iam_policy_document.external_secrets_access_policy.json
  tags        = merge(var.tags, { Name = "ESO-Policy-${var.eks_cluster_name}" })
}

resource "aws_iam_role_policy_attachment" "external_secrets_operator_attach" {
  role       = aws_iam_role.external_secrets_operator.name
  policy_arn = aws_iam_policy.external_secrets_operator_policy.arn
}

# Deploy ExternalSecrets Operator using Helm
resource "helm_release" "external_secrets_operator" {
  name       = "external-secrets"
  repository = "https://charts.external-secrets.io"
  chart      = "external-secrets"
  namespace  = kubernetes_namespace.external_secrets.metadata[0].name
  version    = var.external_secrets_helm_chart_version

  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = aws_iam_role.external_secrets_operator.arn
  }
  set {
    name  = "serviceAccount.name"
    value = "external-secrets" # Matches the SA name in the assume role policy
  }
  # Add other values as needed, e.g., resource requests/limits
  # values = [
  #   yamlencode({
  #     installCRDs = true # Typically true for the first install
  #     # webhook.port = 9443 # Default
  #   })
  # ]

  depends_on = [
    aws_iam_role.external_secrets_operator,
    kubernetes_namespace.external_secrets
  ]
}

# Example SecretStore (commented out, users should define this based on their needs)
/*
resource "kubernetes_manifest" "aws_secret_store_example" {
  manifest = {
    apiVersion = "external-secrets.io/v1beta1"
    kind       = "SecretStore"
    metadata = {
      name      = "aws-secretsmanager-store"
      namespace = var.k8s_namespace_external_secrets # Or application namespace
    }
    spec = {
      provider = {
        aws = {
          service  = "SecretsManager"
          region   = var.aws_region
          auth = {
            jwt = {
              serviceAccountRef = {
                name = "external-secrets" # SA used by ESO, or dedicated SA if ESO is configured to impersonate
                # namespace = var.k8s_namespace_external_secrets # if SecretStore is in a different namespace
              }
            }
          }
        }
      }
    }
  }
  depends_on = [helm_release.external_secrets_operator]
}
*/

# Example ExternalSecret (commented out, users should define this based on their needs)
/*
resource "kubernetes_manifest" "example_app_secret" {
  manifest = {
    apiVersion = "external-secrets.io/v1beta1"
    kind       = "ExternalSecret"
    metadata = {
      name      = "my-app-db-credentials"
      namespace = "my-application-namespace" # Target namespace for the K8s secret
    }
    spec = {
      secretStoreRef = {
        name = "aws-secretsmanager-store" # Reference to the SecretStore
        kind = "SecretStore"             # Or ClusterSecretStore
      }
      target = {
        name           = "my-app-db-secret" # Name of the K8s secret to be created
        creationPolicy = "Owner"
      }
      data = [
        {
          secretKey = "username"       # Key in the K8s secret
          remoteRef = {
            key     = "arn:aws:secretsmanager:REGION:ACCOUNT_ID:secret:my-app/db-creds-XYZ" # ARN of the SM secret
            property = "username"      # JSON key in the SM secret value if it's a JSON blob
          }
        },
        {
          secretKey = "password"
          remoteRef = {
            key     = "arn:aws:secretsmanager:REGION:ACCOUNT_ID:secret:my-app/db-creds-XYZ"
            property = "password"
          }
        }
      ]
    }
  }
  depends_on = [kubernetes_manifest.aws_secret_store_example] # Or ensure SecretStore exists
}
*/

output "external_secrets_operator_iam_role_arn" {
  description = "ARN of the IAM role for the ExternalSecrets Operator."
  value       = aws_iam_role.external_secrets_operator.arn
}

output "external_secrets_namespace" {
  description = "Namespace where ExternalSecrets Operator is deployed."
  value       = kubernetes_namespace.external_secrets.metadata[0].name
}