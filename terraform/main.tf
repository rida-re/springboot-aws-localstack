# Create S3 bucket
resource "aws_s3_bucket" "app_bucket" {
  bucket = "my-bucket"
}

# Create SQS queue
resource "aws_sqs_queue" "app_queue" {
  name = "my-queue"
}

# Create DynamoDB table
resource "aws_dynamodb_table" "items_table" {
  name         = "Items"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }
}


# Create SES email
resource "aws_ses_email_identity" "example" {
  email = "noreply@example.com"
}


