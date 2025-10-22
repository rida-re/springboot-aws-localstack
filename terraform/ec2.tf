data "aws_caller_identity" "current" {}

resource "aws_instance" "spring_app" {
  count         = var.enable_ec2 ? 1 : 0
  ami           = var.ami_id
  instance_type = var.instance_type
  key_name      = var.key_pair_name

  user_data = <<-EOF
              #!/bin/bash
              sudo yum update -y
              sudo amazon-linux-extras install docker -y
              sudo service docker start
              sudo usermod -a -G docker ec2-user
              docker run -d -p 8080:8080 000000000000.dkr.ecr.${var.aws_region}.amazonaws.com/${var.ecr_repo_name}:latest
              EOF

  tags = {
    Name = "springboot-app"
  }
}
