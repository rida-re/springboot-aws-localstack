# springboot-aws-localstack
springboot AWS LocalStack

terraform
- cd terraform
- terraform init
- terrafrom validate
- terraform apply -auto-approve
- terraform destroy -auto-approve

kubernetes
- kubectl apply -f kubernetes/localstack-deployment.yaml
- kubectl rollout status deployment/localstack
- kubectl apply -f kubernetes/springboot-deployment.yaml
- kubectl rollout status deployment/springboot-app

swagger ui
- Start the app (e.g. `mvn spring-boot:run` or your deployment)
- Open `http://localhost:8080/swagger-ui/index.html` to view Swagger UI
- OpenAPI JSON available at `http://localhost:8080/v3/api-docs`

rds (postgres via localstack)
- Ensure LocalStack has `rds` in `SERVICES` (already set in compose/k8s)
- Configure properties in `src/main/resources/application.properties`:
  - `aws.rds.instance`, `aws.rds.dbName`, `aws.rds.username`, `aws.rds.password`
- Create instance: `POST http://localhost:8080/rds/create`
- Check status: `GET http://localhost:8080/rds/status` (wait for `available`)
- Get endpoint: `GET http://localhost:8080/rds/endpoint`
- Optional: auto-create on startup by uncommenting `createRdsInstance()` in `AwsResourceInitializer.init()`

Config SES AWS
- aws --endpoint-url=http://localhost:4566 ses list-identities
- aws --endpoint-url=http://localhost:4566 ses verify-email-identity --email-address noreply@example.com
