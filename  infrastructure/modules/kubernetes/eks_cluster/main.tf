# Purpose: Define a reusable AWS Elastic Kubernetes Service (EKS) cluster.
# LogicDescription: Provisions an EKS control plane, managed node groups (configurable instance types, autoscaling),
# IAM roles for EKS control plane and worker nodes, and configures Kubernetes cluster networking (CNI).
# Integrates with VPC module outputs.

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.10"
    }
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.5"
    }
  }
}

data "aws_caller_identity" "current" {}

# IAM Role for EKS Cluster
resource "aws_iam_role" "eks_cluster_role" {
  name = "${var.cluster_name}-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "eks_cluster_AmazonEKSClusterPolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster_role.name
}

resource "aws_iam_role_policy_attachment" "eks_cluster_AmazonEKSServicePolicy" {
  # AmazonEKSServicePolicy is deprecated, using AmazonEKSVPCResourceController for VPC CNI
  # If other service linked roles are needed, they are typically created by AWS
  # For cluster policy, AmazonEKSClusterPolicy is primary.
  # If you need the cluster to manage resources like ELBs, it requires AmazonEKSVPCResourceController for newer CNI versions.
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSVPCResourceController" # Required for VPC CNI
  role       = aws_iam_role.eks_cluster_role.name
}

# IAM Role for EKS Node Groups
resource "aws_iam_role" "eks_node_group_role" {
  name = "${var.cluster_name}-node-group-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "eks_node_AmazonEKSWorkerNodePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_node_group_role.name
}

resource "aws_iam_role_policy_attachment" "eks_node_AmazonEC2ContainerRegistryReadOnly" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_node_group_role.name
}

resource "aws_iam_role_policy_attachment" "eks_node_AmazonEKS_CNI_Policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_node_group_role.name
}

# Optional: Custom policy for nodes if needed (e.g., S3 access, Secrets Manager)
resource "aws_iam_policy" "eks_node_custom_policy" {
  count = var.node_group_custom_policy_arn == "" && length(var.node_group_custom_policy_statements) > 0 ? 1 : 0
  name  = "${var.cluster_name}-node-custom-policy"
  policy = jsonencode({
    Version   = "2012-10-17"
    Statement = var.node_group_custom_policy_statements
  })
  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "eks_node_custom_policy_attachment" {
  count      = var.node_group_custom_policy_arn == "" && length(var.node_group_custom_policy_statements) > 0 ? 1 : 0
  policy_arn = aws_iam_policy.eks_node_custom_policy[0].arn
  role       = aws_iam_role.eks_node_group_role.name
}

resource "aws_iam_role_policy_attachment" "eks_node_additional_policy_attachment" {
  count      = var.node_group_custom_policy_arn != "" ? 1 : 0
  policy_arn = var.node_group_custom_policy_arn
  role       = aws_iam_role.eks_node_group_role.name
}


# EKS Cluster
resource "aws_eks_cluster" "main" {
  name     = var.cluster_name
  role_arn = aws_iam_role.eks_cluster_role.arn
  version  = var.cluster_version

  vpc_config {
    subnet_ids              = var.subnet_ids
    endpoint_private_access = var.cluster_endpoint_private_access
    endpoint_public_access  = var.cluster_endpoint_public_access
    public_access_cidrs     = var.cluster_public_access_cidrs
    security_group_ids      = compact(concat(var.additional_security_group_ids, [aws_security_group.eks_cluster_sg.id]))
  }

  enabled_cluster_log_types = var.enabled_cluster_log_types

  tags = merge(
    var.tags,
    {
      Name = var.cluster_name
    }
  )

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_AmazonEKSClusterPolicy,
    aws_iam_role_policy_attachment.eks_cluster_AmazonEKSVPCResourceController, # Or AmazonEKSServicePolicy if older
  ]
}

resource "aws_security_group" "eks_cluster_sg" {
  name        = "${var.cluster_name}-cluster-sg"
  description = "Security group for EKS cluster control plane communication with worker nodes"
  vpc_id      = var.vpc_id

  tags = merge(
    var.tags,
    {
      Name = "${var.cluster_name}-cluster-sg"
    }
  )
}

# Managed Node Groups
resource "aws_eks_node_group" "main" {
  for_each = var.managed_node_groups

  cluster_name    = aws_eks_cluster.main.name
  node_group_name = each.key
  node_role_arn   = aws_iam_role.eks_node_group_role.arn
  subnet_ids      = var.subnet_ids # Or specific subnets for node groups: each.value.subnet_ids
  instance_types  = each.value.instance_types
  disk_size       = each.value.disk_size
  ami_type        = each.value.ami_type
  capacity_type   = each.value.capacity_type # ON_DEMAND or SPOT
  release_version = each.value.release_version != "" ? each.value.release_version : aws_eks_cluster.main.version # Ensure this is compatible or use lookup


  scaling_config {
    desired_size = each.value.desired_size
    max_size     = each.value.max_size
    min_size     = each.value.min_size
  }

  update_config {
    max_unavailable_percentage = each.value.max_unavailable_percentage # During updates
  }

  labels = merge(
    var.tags,
    each.value.labels,
    { "node.kubernetes.io/lifecycle" = lower(each.value.capacity_type) }
  )
  tags = merge(
    var.tags,
    each.value.tags,
    { Name = "${var.cluster_name}-${each.key}-node-group" }
  )

  depends_on = [
    aws_iam_role_policy_attachment.eks_node_AmazonEKSWorkerNodePolicy,
    aws_iam_role_policy_attachment.eks_node_AmazonEC2ContainerRegistryReadOnly,
    aws_iam_role_policy_attachment.eks_node_AmazonEKS_CNI_Policy,
  ]
}

# EKS Addons
resource "aws_eks_addon" "vpc_cni" {
  count = var.enable_vpc_cni_addon ? 1 : 0

  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = "vpc-cni"
  addon_version               = var.vpc_cni_addon_version # Specify a version or use default
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"
  tags                        = var.tags
}

resource "aws_eks_addon" "coredns" {
  count = var.enable_coredns_addon ? 1 : 0

  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = "coredns"
  addon_version               = var.coredns_addon_version # Specify a version or use default
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"
  tags                        = var.tags
}

resource "aws_eks_addon" "kube_proxy" {
  count = var.enable_kube_proxy_addon ? 1 : 0

  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = "kube-proxy"
  addon_version               = var.kube_proxy_addon_version # Specify a version or use default
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"
  tags                        = var.tags
}

resource "aws_eks_addon" "ebs_csi_driver" {
  count = var.enable_ebs_csi_driver_addon ? 1 : 0

  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = "aws-ebs-csi-driver"
  addon_version               = var.ebs_csi_driver_addon_version # Specify a version or use default
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"
  service_account_role_arn    = aws_iam_role.ebs_csi_driver_role[0].arn # Requires an IAM role for the CSI driver
  tags                        = var.tags
}

# IAM Role for EBS CSI Driver (IRSA)
resource "aws_iam_role" "ebs_csi_driver_role" {
  count = var.enable_ebs_csi_driver_addon ? 1 : 0
  name  = "${var.cluster_name}-ebs-csi-driver-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:oidc-provider/${replace(aws_eks_cluster.main.identity[0].oidc[0].issuer, "https://", "")}"
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "${replace(aws_eks_cluster.main.identity[0].oidc[0].issuer, "https://", "")}:sub" = "system:serviceaccount:kube-system:ebs-csi-controller-sa"
          }
        }
      }
    ]
  })
  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "ebs_csi_driver_policy_attachment" {
  count      = var.enable_ebs_csi_driver_addon ? 1 : 0
  role       = aws_iam_role.ebs_csi_driver_role[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy" # Managed policy for EBS CSI
}

# OIDC Provider for IRSA
resource "aws_iam_openid_connect_provider" "eks_oidc_provider" {
  count = var.enable_irsa ? 1 : 0

  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.eks_oidc_thumbprint[0].certificates[0].sha1_fingerprint] # Use data source to get thumbprint
  url             = aws_eks_cluster.main.identity[0].oidc[0].issuer

  tags = merge(
    var.tags,
    {
      Name = "${var.cluster_name}-oidc-provider"
    }
  )
}

data "tls_certificate" "eks_oidc_thumbprint" {
  count = var.enable_irsa ? 1 : 0
  url   = aws_eks_cluster.main.identity[0].oidc[0].issuer
}

# This part is typically in the calling module (environment config) or root,
# as provider config depends on cluster creation.
# However, if this module intends to deploy K8s resources (like CRDs, operators via Helm),
# it might need to configure these providers itself, once the cluster is up.
# For now, assuming Kubernetes/Helm resources are managed by other specific files or modules.
# If this module were to deploy something like ExternalSecrets Operator, it would define:
# provider "kubernetes" { ... }
# provider "helm" { ... }
# And then use helm_release for ESO. This logic is in secrets_integration.tf.

# The definition of secrets_integration.tf, apm_config.tf, alerting_config.tf
# implies that this EKS module might also be responsible for deploying Kubernetes resources.
# If so, the Kubernetes and Helm providers must be configured using outputs from this module.
# This creates a cyclic dependency if defined within the same `apply`.
# Usually, these are separate applies or Helm charts are deployed after cluster is ready.
# For this file, we focus on EKS cluster provisioning. The sub-files (secrets_integration, etc.)
# would need the cluster's kubeconfig, endpoint, and CA.