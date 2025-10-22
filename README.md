# springboot-aws-localstack
springboot AWS LocalStack

0. cd terraform
1. terraform init
2. terraform apply -auto-approve

terraform destroy -auto-approve
terrafrom validate


- kubectl apply -f kubernetes/localstack-deployment.yaml
- kubectl rollout status deployment/localstack
- kubectl apply -f kubernetes/springboot-deployment.yaml
- kubectl rollout status deployment/springboot-app
