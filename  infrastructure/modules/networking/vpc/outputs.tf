# Purpose: Expose created resource identifiers and attributes from the VPC module
# for use by other modules.
# LogicDescription: Outputs the VPC ID, public subnet IDs, private subnet IDs,
# NAT Gateway public IPs, Internet Gateway ID, and default security group ID.

output "vpc_id" {
  description = "The ID of the VPC."
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "A list of IDs of the public subnets."
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "A list of IDs of the private subnets."
  value       = aws_subnet.private[*].id
}

output "nat_gateway_public_ips" {
  description = "A list of public IPs of the NAT Gateways."
  value       = aws_eip.nat[*].public_ip
}

output "internet_gateway_id" {
  description = "The ID of the Internet Gateway."
  value       = aws_internet_gateway.main.id
}

output "default_security_group_id" {
  description = "The ID of the default security group for the VPC."
  value       = aws_default_security_group.default.id
}

output "vpc_cidr_block" {
  description = "The CIDR block of the VPC."
  value       = aws_vpc.main.cidr_block
}

output "availability_zones" {
  description = "Availability zones used for subnets."
  value       = var.availability_zones
}