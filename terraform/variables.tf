# ──────────────────────────────
# AWS Configuration
# ──────────────────────────────
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "aws_access_key" {
  description = "AWS Access Key"
  type        = string
  default     = "test"
}

variable "aws_secret_key" {
  description = "AWS Secret Key"
  type        = string
  default     = "test"
}

# ──────────────────────────────
# EC2 Configuration
# ──────────────────────────────
variable "key_pair_name" {
  description = "Name of the existing AWS key pair to use for EC2 SSH access"
  type        = string
  default     = "my-keypair"
}

variable "instance_type" {
  description = "EC2 instance type for Spring Boot app"
  type        = string
  default     = "t3.micro"
}

variable "ami_id" {
  description = "AMI ID for EC2 instance (Amazon Linux 2 or Ubuntu)"
  type        = string
  default     = "ami-0c55b159cbfafe1f0"
}

# ──────────────────────────────
# ECR Configuration
# ──────────────────────────────
variable "ecr_repo_name" {
  description = "Name of the ECR repository for the Spring Boot image"
  type        = string
  default     = "myapp"
}

# ──────────────────────────────
# EKS Configuration
# ──────────────────────────────
variable "cluster_name" {
  description = "EKS cluster name"
  type        = string
  default     = "myapp-cluster"
}

variable "cluster_version" {
  description = "Kubernetes version for EKS"
  type        = string
  default     = "1.30"
}

variable "node_instance_type" {
  description = "EC2 instance type for EKS worker nodes"
  type        = string
  default     = "t3.medium"
}

variable "desired_capacity" {
  description = "Desired number of nodes in the EKS node group"
  type        = number
  default     = 2
}

variable "min_capacity" {
  description = "Minimum number of nodes in the EKS node group"
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Maximum number of nodes in the EKS node group"
  type        = number
  default     = 3
}

# ──────────────────────────────
# Networking
# ──────────────────────────────
variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnets" {
  description = "List of public subnet CIDRs"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnets" {
  description = "List of private subnet CIDRs"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

# ──────────────────────────────
# Tags
# ──────────────────────────────
variable "environment" {
  description = "Deployment environment (e.g., dev, staging, prod)"
  type        = string
  default     = "dev"
}

# ──────────────────────────────
# Feature toggles
# ──────────────────────────────
variable "use_localstack" {
  description = "Use LocalStack endpoints and test credentials"
  type        = bool
  default     = true
}

variable "enable_vpc" {
  description = "Enable VPC networking resources (requires EC2)"
  type        = bool
  default     = false
}

variable "enable_ec2" {
  description = "Enable EC2 instance for the Spring Boot app"
  type        = bool
  default     = false
}

variable "enable_eks" {
  description = "Enable EKS cluster (requires real AWS, not LocalStack)"
  type        = bool
  default     = false
}
