# Purpose: Expose created resource identifiers and attributes from the VPC module
# for use by other modules.
# LogicDescription: Outputs the VPC ID, public subnet IDs, private subnet IDs,
# NAT Gateway public IPs, Internet Gateway ID, and default security group ID.
# ImplementedFeatures: VPC Resource ID Export

output "vpc_id" {
  description = "The ID of the VPC."
  value       = aws_vpc.main.id
}

output "vpc_cidr_block" {
  description = "The CIDR block of the VPC."
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_ids" {
  description = "List of IDs of public subnets."
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "List of IDs of private subnets."
  value       = aws_subnet.private[*].id
}

output "internet_gateway_id" {
  description = "The ID of the Internet Gateway."
  value       = aws_internet_gateway.main.id
}

output "nat_gateway_public_ips" {
  description = "List of public EIPs of NAT Gateways (if enabled)."
  value       = var.enable_nat_gateway ? aws_eip.nat[*].public_ip : []
}

output "nat_gateway_ids" {
  description = "List of IDs of NAT Gateways (if enabled)."
  value       = var.enable_nat_gateway ? aws_nat_gateway.main[*].id : []
}

output "default_security_group_id" {
  description = "The ID of the default security group."
  value       = aws_default_security_group.default.id
}