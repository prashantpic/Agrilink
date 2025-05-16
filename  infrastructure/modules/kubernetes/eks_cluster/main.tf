# Purpose: Define a reusable AWS Elastic Kubernetes Service (EKS) cluster.
# LogicDescription: Provisions an EKS control plane, managed node groups
# (configurable instance types, autoscaling), IAM roles for EKS control plane
# and worker nodes, and configures Kubernetes cluster networking (CNI).
# Integrates with VPC module outputs.
# ImplementedFeatures: EKS Control Plane Provisioning, Managed Node Group Configuration,
# IAM Role for EKS Setup, EKS Addons Configuration (e.g., VPC CNI, CoreDNS, kube-proxy).

locals {
  cluster_name = "${var.project_name}-${var.environment}-eks"
  tags = merge(
    var.tags,
    {
      "Name"                                = local.cluster_name
      "eksctl.cluster.k8s.io/v1alpha1/cluster-name" = local.cluster_name # For compatibility with eksctl if used elsewhere
      "kubernetes.io/cluster/${local.cluster_name}" = "owned"
    }
  )
}

data "aws_caller_identity" "current" {}

resource "aws_iam_role" "eks_cluster_role" {
  name = "${local.cluster_name}-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = local.tags
}

resource "aws_iam_role_policy_attachment" "eks_cluster_AmazonEKSClusterPolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster_role.name
}

resource "aws_iam_role_policy_attachment" "eks_cluster_AmazonEKSServicePolicy" {
  # AmazonEKSServicePolicy is deprecated, use AmazonEKSClusterPolicy and AmazonEKSVPCResourceController
  # However, some older documentation or tools might still expect it.
  # For broader compatibility, ensure VPCResourceController is attached if you manage VPC resources via EKS.
  # Sticking to defined policies unless specific needs arise.
  # Typically AmazonEKSVPCResourceController is also needed for VPC CNI.
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSVPCResourceController" # Recommended for VPC CNI
  role       = aws_iam_role.eks_cluster_role.name
}


resource "aws_eks_cluster" "main" {
  name     = local.cluster_name
  role_arn = aws_iam_role.eks_cluster_role.arn
  version  = var.kubernetes_version

  vpc_config {
    subnet_ids             = var.private_subnet_ids # EKS control plane typically in private subnets
    public_access_cidrs    = var.eks_public_access_cidrs
    endpoint_private_access = var.eks_endpoint_private_access
    endpoint_public_access  = var.eks_endpoint_public_access
    security_group_ids     = [aws_security_group.eks_cluster_sg.id]
  }

  enabled_cluster_log_types = var.enabled_cluster_log_types

  tags = local.tags

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_AmazonEKSClusterPolicy,
    aws_iam_role_policy_attachment.eks_cluster_AmazonEKSServicePolicy, # or AmazonEKSVPCResourceController
  ]
}

resource "aws_security_group" "eks_cluster_sg" {
  name        = "${local.cluster_name}-cluster-sg"
  description = "Security group for EKS cluster control plane communication with worker nodes."
  vpc_id      = var.vpc_id

  tags = local.tags
}

# Allow worker nodes to communicate with the EKS control plane API server
resource "aws_security_group_rule" "cluster_ingress_node_https" {
  description              = "Allow worker nodes to communicate with the EKS control plane"
  from_port                = 443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.eks_cluster_sg.id
  source_security_group_id = aws_security_group.eks_node_sg.id # From Node Group SG
  to_port                  = 443
  type                     = "ingress"
}

# Allow EKS control plane to communicate to nodes on ephemeral ports for kubectl exec/logs
resource "aws_security_group_rule" "cluster_egress_node_kubelet" {
  description              = "Allow EKS control plane to communicate to Kubelet on nodes"
  from_port                = 10250 # Kubelet secure port
  protocol                 = "tcp"
  security_group_id        = aws_security_group.eks_cluster_sg.id
  source_security_group_id = aws_security_group.eks_node_sg.id # To Node Group SG
  to_port                  = 10250
  type                     = "egress"
}

# Allow EKS control plane to communicate with nodes for CNI related traffic (if needed)
# For example, if using Calico and it requires direct API server communication from nodes on specific ports.
# Default VPC CNI communication is generally handled by existing rules.

resource "aws_iam_role" "eks_node_role" {
  name = "${local.cluster_name}-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = local.tags
}

resource "aws_iam_role_policy_attachment" "eks_node_AmazonEKSWorkerNodePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_node_role.name
}

resource "aws_iam_role_policy_attachment" "eks_node_AmazonEC2ContainerRegistryReadOnly" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_node_role.name
}

resource "aws_iam_role_policy_attachment" "eks_node_AmazonEKS_CNI_Policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_node_role.name
}

resource "aws_security_group" "eks_node_sg" {
  name        = "${local.cluster_name}-node-sg"
  description = "Security group for EKS worker nodes."
  vpc_id      = var.vpc_id

  tags = local.tags
}

# Ingress rules for node group SG
# Allow all traffic from the EKS Cluster SG
resource "aws_security_group_rule" "node_ingress_cluster" {
  description              = "Allow EKS control plane to communicate with worker nodes"
  from_port                = 0
  protocol                 = "-1" # All traffic
  security_group_id        = aws_security_group.eks_node_sg.id
  source_security_group_id = aws_security_group.eks_cluster_sg.id
  to_port                  = 0
  type                     = "ingress"
}

# Allow traffic from other nodes in the same node group SG (for pod-to-pod communication)
resource "aws_security_group_rule" "node_ingress_self" {
  description              = "Allow nodes within the same SG to communicate"
  from_port                = 0
  protocol                 = "-1"
  security_group_id        = aws_security_group.eks_node_sg.id
  source_security_group_id = aws_security_group.eks_node_sg.id # Self
  to_port                  = 0
  type                     = "ingress"
}

# Egress rules for node group SG
resource "aws_security_group_rule" "node_egress_all" {
  description       = "Allow all outbound traffic from worker nodes"
  from_port         = 0
  protocol          = "-1"
  security_group_id = aws_security_group.eks_node_sg.id
  cidr_blocks       = ["0.0.0.0/0"]
  to_port           = 0
  type              = "egress"
}

resource "aws_eks_node_group" "main" {
  for_each = var.node_groups

  cluster_name    = aws_eks_cluster.main.name
  node_group_name = each.key
  node_role_arn   = aws_iam_role.eks_node_role.arn
  subnet_ids      = var.private_subnet_ids # Worker nodes usually in private subnets
  
  instance_types = each.value.instance_types
  disk_size      = each.value.disk_size
  ami_type       = each.value.ami_type
  capacity_type  = each.value.capacity_type # ON_DEMAND or SPOT

  scaling_config {
    desired_size = each.value.desired_size
    max_size     = each.value.max_size
    min_size     = each.value.min_size
  }

  update_config {
    max_unavailable_percentage = each.value.max_unavailable_percentage # Or max_unavailable
  }

  labels = merge(
    var.tags,
    each.value.labels,
    {
      "eks.amazonaws.com/nodegroup" = each.key
      "eks.amazonaws.com/capacityType" = each.value.capacity_type
    }
  )
  
  tags = merge(
    local.tags,
    {
      "Name" = "${local.cluster_name}-${each.key}-node"
      "eks.amazonaws.com/nodegroup-name" = each.key # EKS specific tag
    }
  )
  
  depends_on = [
    aws_iam_role_policy_attachment.eks_node_AmazonEKSWorkerNodePolicy,
    aws_iam_role_policy_attachment.eks_node_AmazonEC2ContainerRegistryReadOnly,
    aws_iam_role_policy_attachment.eks_node_AmazonEKS_CNI_Policy,
    aws_eks_cluster.main
  ]
  
  # Ensure node security group allows communication with the cluster security group
  # This is primarily handled by the SG rules defined above.
  # If specific launch template is used, ensure its SG is configured correctly or use remote_access.
  remote_access {
     ec2_ssh_key               = var.ec2_ssh_key_name
     source_security_group_ids = var.node_ssh_access_sgs
  }
}


resource "aws_eks_addon" "vpc_cni" {
  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = "vpc-cni"
  addon_version               = var.eks_addon_versions.vpc_cni
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"
  tags                        = local.tags
}

resource "aws_eks_addon" "coredns" {
  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = "coredns"
  addon_version               = var.eks_addon_versions.coredns
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"
  tags                        = local.tags
}

resource "aws_eks_addon" "kube_proxy" {
  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = "kube-proxy"
  addon_version               = var.eks_addon_versions.kube_proxy
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"
  tags                        = local.tags
}

resource "aws_iam_openid_connect_provider" "main" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [var.eks_oidc_provider_thumbprint] # This needs to be fetched or a static known one
  url             = aws_eks_cluster.main.identity[0].oidc[0].issuer

  tags = local.tags
}