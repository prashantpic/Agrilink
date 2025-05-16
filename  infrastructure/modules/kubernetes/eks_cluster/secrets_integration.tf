# Purpose: Securely manage and inject secrets from AWS Secrets Manager into Kubernetes pods within the EKS cluster.
# LogicDescription: Deploys the ExternalSecrets Operator (ESO) via Helm chart.
# Configures ESO to integrate with AWS Secrets Manager by setting up necessary IAM roles and permissions (IRSA).
# REQ-8-011: Securely manage and inject secrets from AWS Secrets Manager into Kubernetes pods.

variable "eks_cluster_name" {
  description = "The name of the EKS cluster."
  type        = string
}

variable "eks_oidc_provider_arn" {
  description = "ARN of the OIDC provider for the EKS cluster."
  type        = string
}

variable "aws_region" {
  description = "AWS region for resource deployment."
  type        = string
}

variable "external_secrets_operator_helm_chart_version" {
  description = "Version of the ExternalSecrets Operator Helm chart."
  type        = string
  default     = "0.9.13" # Check for the latest stable version
}

variable "external_secrets_operator_namespace" {
  description = "Kubernetes namespace to deploy ExternalSecrets Operator into."
  type        = string
  default     = "external-secrets"
}

variable "external_secrets_service_account_name" {
  description = "Name of the Kubernetes service account for ExternalSecrets Operator."
  type        = string
  default     = "external-secrets"
}

variable "tags" {
  description = "A map of tags to add to all resources."
  type        = map(string)
  default     = {}
}

data "aws_iam_policy_document" "external_secrets_operator_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"

    principals {
      type        = "Federated"
      identifiers = [var.eks_oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${replace(var.eks_oidc_provider_arn, "oidc-provider/", "oidc.eks.${var.aws_region}.amazonaws.com/id/")}:sub"
      values   = ["system:serviceaccount:${var.external_secrets_operator_namespace}:${var.external_secrets_service_account_name}"]
    }
    condition {
      test     = "StringEquals"
      variable = "${replace(var.eks_oidc_provider_arn, "oidc-provider/", "oidc.eks.${var.aws_region}.amazonaws.com/id/")}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "external_secrets_operator_role" {
  name               = "${var.eks_cluster_name}-external-secrets-operator-role"
  assume_role_policy = data.aws_iam_policy_document.external_secrets_operator_assume_role_policy.json
  tags               = merge(var.tags, { Name = "${var.eks_cluster_name}-external-secrets-operator-role" })
}

data "aws_iam_policy_document" "external_secrets_operator_policy_doc" {
  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecrets" # Required for wildcard matching or discovery, use with caution
    ]
    resources = ["*"] # TODO: Scope down to specific secret ARNs or patterns (e.g., "arn:aws:secretsmanager:${var.aws_region}:${data.aws_caller_identity.current.account_id}:secret:${var.project_name}-${var.environment}-*")
  }
  statement {
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = ["*"] # TODO: Scope down to specific KMS key ARNs used for encrypting secrets if secrets are KMS encrypted
    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"
      values   = ["secretsmanager.${var.aws_region}.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "external_secrets_operator_policy" {
  name        = "${var.eks_cluster_name}-external-secrets-operator-policy"
  description = "IAM policy for ExternalSecrets Operator to access AWS Secrets Manager."
  policy      = data.aws_iam_policy_document.external_secrets_operator_policy_doc.json
  tags        = merge(var.tags, { Name = "${var.eks_cluster_name}-external-secrets-operator-policy" })
}

resource "aws_iam_role_policy_attachment" "external_secrets_operator_attach" {
  role       = aws_iam_role.external_secrets_operator_role.name
  policy_arn = aws_iam_policy.external_secrets_operator_policy.arn
}

resource "kubernetes_namespace_v1" "external_secrets" {
  metadata {
    name = var.external_secrets_operator_namespace
    labels = {
      name = var.external_secrets_operator_namespace
    }
  }
}

resource "helm_release" "external_secrets_operator" {
  name       = "external-secrets"
  repository = "https://charts.external-secrets.io"
  chart      = "external-secrets"
  namespace  = kubernetes_namespace_v1.external_secrets.metadata[0].name
  version    = var.external_secrets_operator_helm_chart_version

  set {
    name  = "serviceAccount.name"
    value = var.external_secrets_service_account_name
  }
  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = aws_iam_role.external_secrets_operator_role.arn
  }
  set {
    name  = "installCRDs"
    value = "true"
  }
  # Add other necessary Helm values
  # e.g. resource requests/limits for ESO controller
  # set {
  #   name  = "resources.requests.cpu"
  #   value = "100m"
  # }
  # set {
  #   name  = "resources.requests.memory"
  #   value = "128Mi"
  # }

  depends_on = [
    aws_iam_role_policy_attachment.external_secrets_operator_attach,
    kubernetes_namespace_v1.external_secrets
  ]
}

# Example SecretStore (commented out - to be defined by applications/environments)
/*
resource "kubernetes_manifest" "example_aws_secret_store" {
  # Ensure Helm release for CRDs is complete
  depends_on = [helm_release.external_secrets_operator]

  manifest = {
    "apiVersion" = "external-secrets.io/v1beta1"
    "kind"       = "SecretStore"
    "metadata" = {
      "name"      = "aws-secretsmanager-store"
      "namespace" = "default" # Or application-specific namespace
    }
    "spec" = {
      "provider" = {
        "aws" = {
          "service" = "SecretsManager"
          "region"  = var.aws_region
          "auth" = {
            "jwt" = {
              "serviceAccountRef" = {
                "name" = "default" # Service account of the application pod
              }
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_manifest" "example_external_secret" {
  # Ensure Helm release for CRDs is complete and SecretStore exists
  depends_on = [helm_release.external_secrets_operator, kubernetes_manifest.example_aws_secret_store]

  manifest = {
    "apiVersion" = "external-secrets.io/v1beta1"
    "kind"       = "ExternalSecret"
    "metadata" = {
      "name"      = "example-db-credentials"
      "namespace" = "default" # Or application-specific namespace
    }
    "spec" = {
      "refreshInterval" = "1h"
      "secretStoreRef" = {
        "name" = "aws-secretsmanager-store"
        "kind" = "SecretStore"
      }
      "target" = {
        "name"              = "db-credentials" # Name of the Kubernetes Secret to be created
        "creationPolicy"    = "Owner"
        "template" = {
          "engineVersion" = "v2"
          "data" = {
            "username" = "{{ .username }}"
            "password" = "{{ .password }}"
          }
        }
      }
      "data" = [
        {
          "secretKey" = "username"
          "remoteRef" = {
            "key"     = "my-app/dev/db-credentials" # Path to secret in AWS Secrets Manager
            "property" = "username"
          }
        },
        {
          "secretKey" = "password"
          "remoteRef" = {
            "key"     = "my-app/dev/db-credentials" # Path to secret in AWS Secrets Manager
            "property" = "password"
          }
        }
      ]
    }
  }
}
*/

output "external_secrets_operator_role_arn" {
  description = "ARN of the IAM role for ExternalSecrets Operator."
  value       = aws_iam_role.external_secrets_operator_role.arn
}

output "external_secrets_operator_namespace" {
  description = "Kubernetes namespace where ExternalSecrets Operator is deployed."
  value       = var.external_secrets_operator_namespace
}