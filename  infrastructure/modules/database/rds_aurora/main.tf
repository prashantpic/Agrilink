# Purpose: Provision scalable and resilient AWS RDS or Aurora database clusters.
# LogicDescription: Creates RDS instances or Aurora clusters. Configures instance class,
# storage type, multi-AZ deployment, read replicas, backup retention,
# encryption at rest, security groups, DB subnet groups, and parameter groups.
# ImplementedFeatures: RDS/Aurora Cluster Provisioning, Instance Configuration,
# High Availability Setup, Backup Configuration, Security Group Management.

locals {
  db_name_prefix = "${var.project_name}-${var.environment}"
  tags           = var.tags
}

resource "aws_db_subnet_group" "main" {
  name       = "${local.db_name_prefix}-sng"
  subnet_ids = var.private_subnet_ids
  tags = merge(local.tags, {
    Name = "${local.db_name_prefix}-sng"
  })
}

resource "aws_security_group" "db_sg" {
  name        = "${local.db_name_prefix}-db-sg"
  description = "Security group for RDS/Aurora database access"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Allow application SGs to access DB"
    from_port       = var.db_port
    to_port         = var.db_port
    protocol        = "tcp"
    security_groups = var.application_security_group_ids
  }

  # Potentially add ingress for bastion/VPN if direct access is needed for admin
  # ingress {
  #   description = "Allow Bastion host to access DB"
  #   from_port   = var.db_port
  #   to_port     = var.db_port
  #   protocol    = "tcp"
  #   cidr_blocks = [var.bastion_cidr_block] # Example
  # }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"] # Or restrict to specific IPs/SGs if needed
  }

  tags = merge(local.tags, {
    Name = "${local.db_name_prefix}-db-sg"
  })
}

# --- Aurora PostgreSQL Cluster ---
resource "aws_rds_cluster" "aurora_pg" {
  count = var.engine_mode == "provisioned" && substr(var.engine, 0, 13) == "aurora-postgr" ? 1 : 0

  cluster_identifier              = "${local.db_name_prefix}-aurora-pg"
  engine                          = var.engine
  engine_version                  = var.engine_version
  engine_mode                     = var.engine_mode # provisioned, serverless
  database_name                   = var.db_name
  master_username                 = var.master_username
  master_password                 = var.master_password_secret_arn != "" ? null : var.master_password # Password from SM preferred
  manage_master_user_password     = var.master_password_secret_arn != "" ? true : false
  master_user_secret_kms_key_id   = var.master_password_secret_arn != "" ? var.kms_key_id_for_secrets : null
  
  db_subnet_group_name            = aws_db_subnet_group.main.name
  vpc_security_group_ids          = [aws_security_group.db_sg.id]
  
  backup_retention_period         = var.backup_retention_period
  preferred_backup_window         = var.preferred_backup_window
  preferred_maintenance_window    = var.preferred_maintenance_window
  
  storage_encrypted               = var.storage_encrypted
  kms_key_id                      = var.storage_encrypted ? var.kms_key_id_for_storage : null
  
  skip_final_snapshot             = var.skip_final_snapshot
  deletion_protection             = var.deletion_protection
  
  copy_tags_to_snapshot           = true
  allow_major_version_upgrade     = var.allow_major_version_upgrade
  apply_immediately               = var.apply_immediately

  # Aurora specific settings
  serverlessv2_scaling_configuration {
    min_capacity = var.engine_mode == "serverless" ? var.serverless_min_acu : null
    max_capacity = var.engine_mode == "serverless" ? var.serverless_max_acu : null
  }

  enabled_cloudwatch_logs_exports = var.enabled_cloudwatch_logs_exports

  tags = merge(local.tags, {
    Name = "${local.db_name_prefix}-aurora-pg-cluster"
  })
}

resource "aws_rds_cluster_instance" "aurora_pg_instances" {
  count = var.engine_mode == "provisioned" && substr(var.engine, 0, 13) == "aurora-postgr" ? var.instance_count : 0

  identifier              = "${local.db_name_prefix}-aurora-pg-instance-${count.index}"
  cluster_identifier      = aws_rds_cluster.aurora_pg[0].id
  instance_class          = var.instance_class
  engine                  = var.engine # Must match cluster
  engine_version          = var.engine_version # Must match cluster
  
  publicly_accessible     = false
  db_subnet_group_name    = aws_db_subnet_group.main.name # Not needed for cluster instances but good practice
  
  # promotion_tier controls failover priority for reader instances
  promotion_tier          = count.index == 0 ? 0 : count.index + 1 # Writer is tier 0 (implicitly)
  
  apply_immediately       = var.apply_immediately
  auto_minor_version_upgrade = var.auto_minor_version_upgrade

  tags = merge(local.tags, {
    Name = "${local.db_name_prefix}-aurora-pg-instance-${count.index}"
  })
}


# --- Standard RDS PostgreSQL Instance ---
resource "aws_db_instance" "rds_pg" {
  count = var.engine_mode == "provisioned" && substr(var.engine, 0, 10) == "postgres" && substr(var.engine, 0, 6) != "aurora" ? 1 : 0

  identifier                   = "${local.db_name_prefix}-rds-pg"
  engine                       = var.engine
  engine_version               = var.engine_version
  instance_class               = var.instance_class
  
  allocated_storage            = var.allocated_storage
  max_allocated_storage        = var.max_allocated_storage # For storage autoscaling
  storage_type                 = var.storage_type
  iops                         = var.storage_type == "io1" || var.storage_type == "gp3" ? var.iops : null

  db_name                      = var.db_name
  username                     = var.master_username
  password                     = var.master_password_secret_arn != "" ? null : var.master_password
  manage_master_user_password  = var.master_password_secret_arn != "" ? true : false
  master_user_secret_kms_key_id= var.master_password_secret_arn != "" ? var.kms_key_id_for_secrets : null

  db_subnet_group_name         = aws_db_subnet_group.main.name
  vpc_security_group_ids       = [aws_security_group.db_sg.id]
  
  multi_az                     = var.multi_az
  publicly_accessible          = false
  
  backup_retention_period      = var.backup_retention_period
  preferred_backup_window      = var.preferred_backup_window
  preferred_maintenance_window = var.preferred_maintenance_window
  
  storage_encrypted            = var.storage_encrypted
  kms_key_id                   = var.storage_encrypted ? var.kms_key_id_for_storage : null
  
  skip_final_snapshot          = var.skip_final_snapshot
  deletion_protection          = var.deletion_protection
  
  copy_tags_to_snapshot        = true
  allow_major_version_upgrade  = var.allow_major_version_upgrade
  apply_immediately            = var.apply_immediately
  auto_minor_version_upgrade   = var.auto_minor_version_upgrade

  enabled_cloudwatch_logs_exports = var.enabled_cloudwatch_logs_exports
  
  tags = merge(local.tags, {
    Name = "${local.db_name_prefix}-rds-pg-instance"
  })
}

# Placeholder for parameter groups if customization is needed
# resource "aws_db_parameter_group" "default" {
#   name   = "${local.db_name_prefix}-pg-${replace(var.engine_version, ".", "-")}"
#   family = var.engine == "aurora-postgresql" ? "aurora-postgresql${split(".", var.engine_version)[0]}" : "${var.engine}${split(".", var.engine_version)[0]}" # e.g. postgres13 or aurora-postgresql13
#
#   parameter {
#     name  = "log_connections"
#     value = "1"
#   }
#
#   tags = merge(local.tags, {
#     Name = "${local.db_name_prefix}-pg"
#   })
# }
#
# resource "aws_rds_cluster_parameter_group" "aurora_default" {
#   count = var.engine == "aurora-postgresql" ? 1 : 0
#   name   = "${local.db_name_prefix}-aurora-cluster-pg-${replace(var.engine_version, ".", "-")}"
#   family = "aurora-postgresql${split(".", var.engine_version)[0]}" # e.g. aurora-postgresql13
#
#   parameter {
#     name  = "log_statement"
#     value = "ddl"
#   }
#
#   tags = merge(local.tags, {
#     Name = "${local.db_name_prefix}-aurora-cluster-pg"
#   })
# }

# Note: Associating parameter groups:
# For aws_rds_cluster: parameter_group_name = aws_rds_cluster_parameter_group.aurora_default[0].name
# For aws_db_instance (non-Aurora): parameter_group_name = aws_db_parameter_group.default.name
# For aws_rds_cluster_instance: (uses cluster parameter group by default, can specify db_parameter_group_name for instance specific overrides)