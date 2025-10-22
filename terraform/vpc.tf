# ──────────────────────────────
# Create a VPC for EKS & EC2
# ──────────────────────────────
module "vpc" {
  count   = var.enable_vpc ? 1 : 0
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.8.1"

  name = "${var.environment}-vpc"
  cidr = var.vpc_cidr

  azs             = ["${var.aws_region}a", "${var.aws_region}b"]
  private_subnets = var.private_subnets
  public_subnets  = var.public_subnets

  enable_nat_gateway   = true
  single_nat_gateway   = true
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.environment}-vpc"
    Environment = var.environment
  }
}
