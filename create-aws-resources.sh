#!/bin/bash

ENDPOINT=http://localhost:4566

# S3 bucket
aws --endpoint-url=$ENDPOINT s3 mb s3://my-bucket

# SQS queue
aws --endpoint-url=$ENDPOINT sqs create-queue --queue-name my-queue

# DynamoDB table
aws --endpoint-url=$ENDPOINT dynamodb create-table \
    --table-name Items \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST
