output "s3_bucket_name" {
  value = aws_s3_bucket.app_bucket.bucket
}

output "sqs_queue_url" {
  value = aws_sqs_queue.app_queue.id
}

output "dynamodb_table_name" {
  value = aws_dynamodb_table.items_table.name
}

output "ec2_public_ip" {
  value = try(aws_instance.spring_app[0].public_ip, null)
}

output "eks_cluster_endpoint" {
  value = try(module.eks[0].cluster_endpoint, null)
}
