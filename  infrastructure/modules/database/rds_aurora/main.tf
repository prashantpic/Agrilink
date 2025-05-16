# Purpose: Provision scalable and resilient AWS RDS or Aurora database clusters.
# LogicDescription: Creates RDS instances or Aurora clusters (MySQL/PostgreSQL compatible).
# Configures instance class, storage type, multi-AZ deployment, read replicas,
# backup retention, encryption at rest, security groups, DB subnet groups, and parameter groups.

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
}

# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-${var.db_name}-sng"
  subnet_ids = var.private_subnet_ids
  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.db_name}-sng"
    }
  )
}

# DB Security Group
resource "aws_security_group" "db_sg" {
  name        = "${var.project_name}-${var.environment}-${var.db_name}-sg"
  description = "Controls access to the ${var.db_name} database"
  vpc_id      = var.vpc_id

  # Ingress rules should be specific, e.g., from application security group
  # This is a placeholder, customize as needed.
  ingress {
    description     = "Allow traffic from application security group"
    from_port       = var.db_port
    to_port         = var.db_port
    protocol        = "tcp"
    security_groups = var.allowed_security_group_ids # Input variable
  }

  # Allow all outbound traffic by default, or restrict as necessary
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.db_name}-sg"
    }
  )
}

# DB Parameter Group (Optional, but recommended for customization)
resource "aws_db_parameter_group" "main" {
  count = var.create_db_parameter_group ? 1 : 0

  name   = "${var.project_name}-${var.environment}-${var.db_name}-${var.engine}"
  family = var.parameter_group_family # e.g., "aurora-postgresql13", "postgres14", "mysql8.0"

  dynamic "parameter" {
    for_each = var.db_parameters
    content {
      name         = parameter.value.name
      value        = parameter.value.value
      apply_method = lookup(parameter.value, "apply_method", "pending-reboot") # or "immediate"
    }
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.db_name}-${var.engine}-pg"
    }
  )
}

# RDS Cluster (for Aurora)
resource "aws_rds_cluster" "aurora_cluster" {
  count = var.is_aurora ? 1 : 0

  cluster_identifier              = "${var.project_name}-${var.environment}-${var.db_name}"
  engine                          = var.engine # e.g., "aurora-postgresql" or "aurora-mysql"
  engine_version                  = var.engine_version
  engine_mode                     = var.aurora_engine_mode # "provisioned", "serverless"
  availability_zones              = var.availability_zones
  database_name                   = var.initial_database_name
  master_username                 = var.master_username
  master_password                 = var.manage_master_user_password ? null : var.master_password_plain # If manage_master_user_password is true, AWS SM handles it
  manage_master_user_password     = var.manage_master_user_password
  master_user_secret_kms_key_id   = var.manage_master_user_password ? var.secrets_kms_key_id : null
  db_subnet_group_name            = aws_db_subnet_group.main.name
  vpc_security_group_ids          = [aws_security_group.db_sg.id]
  db_cluster_parameter_group_name = var.create_db_parameter_group ? aws_db_parameter_group.main[0].name : var.db_cluster_parameter_group_name
  skip_final_snapshot             = var.skip_final_snapshot
  final_snapshot_identifier       = var.skip_final_snapshot ? null : "${var.project_name}-${var.environment}-${var.db_name}-final-snapshot"
  backup_retention_period         = var.backup_retention_period
  preferred_backup_window         = var.preferred_backup_window
  preferred_maintenance_window    = var.preferred_maintenance_window
  storage_encrypted               = var.storage_encrypted
  kms_key_id                      = var.storage_encrypted ? var.kms_key_id : null
  deletion_protection             = var.deletion_protection
  copy_tags_to_snapshot           = true
  iam_database_authentication_enabled = var.iam_database_authentication_enabled
  # For serverless v2
  serverlessv2_scaling_configuration {
    min_capacity = var.aurora_engine_mode == "serverless" ? var.serverless_min_capacity : null
    max_capacity = var.aurora_engine_mode == "serverless" ? var.serverless_max_capacity : null
  }
  allow_major_version_upgrade     = var.allow_major_version_upgrade
  apply_immediately               = var.apply_immediately

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.db_name}-cluster"
    }
  )
}

resource "aws_rds_cluster_instance" "aurora_instance" {
  count = var.is_aurora ? var.aurora_instance_count : 0

  identifier              = "${var.project_name}-${var.environment}-${var.db_name}-instance-${count.index}"
  cluster_identifier      = aws_rds_cluster.aurora_cluster[0].id
  instance_class          = var.instance_class
  engine                  = var.engine
  engine_version          = var.engine_version
  publicly_accessible     = var.publicly_accessible
  db_subnet_group_name    = aws_db_subnet_group.main.name
  db_parameter_group_name = var.create_db_parameter_group ? aws_db_parameter_group.main[0].name : var.db_parameter_group_name # For Aurora instances, parameter group is at cluster level generally
  # For Aurora, parameters are usually managed at the cluster level.
  # If instance-specific parameter group is needed, it would be aws_db_parameter_group.
  promotion_tier          = count.index == 0 ? 0 : 1 # First instance is writer, others readers
  apply_immediately       = var.apply_immediately
  copy_tags_to_snapshot   = true

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.db_name}-instance-${count.index}"
    }
  )
}


# RDS Instance (for non-Aurora)
resource "aws_db_instance" "rds_instance" {
  count = !var.is_aurora ? 1 : 0 # For primary instance. Replicas handled separately if needed.

  identifier                = "${var.project_name}-${var.environment}-${var.db_name}"
  engine                    = var.engine # e.g., "postgres", "mysql"
  engine_version            = var.engine_version
  instance_class            = var.instance_class
  allocated_storage         = var.allocated_storage
  max_allocated_storage     = var.max_allocated_storage # For storage autoscaling
  storage_type              = var.storage_type          # e.g., "gp2", "gp3", "io1"
  iops                      = var.storage_type == "io1" || var.storage_type == "gp3" ? var.iops : null
  storage_throughput        = var.storage_type == "gp3" ? var.storage_throughput : null
  db_name                   = var.initial_database_name
  username                  = var.master_username
  password                  = var.manage_master_user_password ? null : var.master_password_plain
  manage_master_user_password = var.manage_master_user_password
  master_user_secret_kms_key_id = var.manage_master_user_password ? var.secrets_kms_key_id : null
  db_subnet_group_name      = aws_db_subnet_group.main.name
  vpc_security_group_ids    = [aws_security_group.db_sg.id]
  parameter_group_name      = var.create_db_parameter_group ? aws_db_parameter_group.main[0].name : var.db_parameter_group_name
  multi_az                  = var.multi_az
  publicly_accessible       = var.publicly_accessible
  skip_final_snapshot       = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.project_name}-${var.environment}-${var.db_name}-final-snapshot"
  backup_retention_period   = var.backup_retention_period
  preferred_backup_window   = var.preferred_backup_window
  preferred_maintenance_window = var.preferred_maintenance_window
  storage_encrypted         = var.storage_encrypted
  kms_key_id                = var.storage_encrypted ? var.kms_key_id : null
  deletion_protection       = var.deletion_protection
  copy_tags_to_snapshot     = true
  iam_database_authentication_enabled = var.iam_database_authentication_enabled
  allow_major_version_upgrade = var.allow_major_version_upgrade
  apply_immediately         = var.apply_immediately

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.db_name}-instance"
    }
  )
}

# RDS Read Replicas (for non-Aurora)
resource "aws_db_instance" "rds_replica_instance" {
  count = !var.is_aurora && var.rds_replica_count > 0 ? var.rds_replica_count : 0

  identifier                = "${var.project_name}-${var.environment}-${var.db_name}-replica-${count.index}"
  replicate_source_db       = aws_db_instance.rds_instance[0].identifier # Source is the primary instance
  instance_class            = var.instance_class # Can be different for replicas
  # engine, engine_version, storage_type, etc., are inherited from source or can be overridden if API allows
  publicly_accessible       = var.publicly_accessible_replicas
  skip_final_snapshot       = true # Replicas typically don't have final snapshots
  multi_az                  = var.replica_multi_az # Replicas can also be Multi-AZ
  apply_immediately         = var.apply_immediately
  copy_tags_to_snapshot     = true

  # Security groups, parameter groups for replicas can be specified if different
  vpc_security_group_ids    = [aws_security_group.db_sg.id] # Same SG or a different one
  db_subnet_group_name      = aws_db_subnet_group.main.name

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.db_name}-replica-${count.index}"
    }
  )
}