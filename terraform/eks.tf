module "eks" {
  count   = var.enable_eks ? 1 : 0
  source  = "terraform-aws-modules/eks/aws"
  version = "21.6.1"

  # General cluster config
  name               = var.cluster_name
  kubernetes_version = var.cluster_version

  # VPC settings
  vpc_id     = try(module.vpc[0].vpc_id, null)
  subnet_ids = try(module.vpc[0].private_subnets, null)

  # EKS-managed node group
  eks_managed_node_groups = {
    default = {
      instance_types = [var.node_instance_type]
      min_size       = var.min_capacity
      desired_size   = var.desired_capacity
      max_size       = var.max_capacity
    }
  }

  # IAM roles and tags
  enable_irsa = true

  tags = {
    Environment = var.environment
    Terraform   = "true"
  }
}
