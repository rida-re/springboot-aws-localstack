package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

@Component
public class AwsResourceInitializer {

    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.sqs.queue}")
    private String queueName;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    public AwsResourceInitializer(S3Client s3Client, SqsClient sqsClient, DynamoDbClient dynamoDbClient) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
        this.dynamoDbClient = dynamoDbClient;
    }

    @PostConstruct
    public void init() {
       // createS3Bucket();
       // createSqsQueue();
       // createDynamoTable();
    }

    private void createS3Bucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            System.out.println("✅ S3 bucket already exists: " + bucketName);
        } catch (S3Exception e) {
            System.out.println("⚙️ Creating S3 bucket: " + bucketName);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            System.out.println("✅ Created S3 bucket: " + bucketName);
        }
    }

    private void createSqsQueue() {
        try {
            sqsClient.getQueueUrl(b -> b.queueName(queueName));
            System.out.println("✅ SQS queue already exists: " + queueName);
        } catch (QueueDoesNotExistException e) {
            System.out.println("⚙️ Creating SQS queue: " + queueName);
            sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
            System.out.println("✅ Created SQS queue: " + queueName);
        }
    }

    private void createDynamoTable() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
            System.out.println("✅ DynamoDB table already exists: " + tableName);
        } catch (software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException e) {
            System.out.println("⚙️ Creating DynamoDB table: " + tableName);
            dynamoDbClient.createTable(CreateTableRequest.builder()
                                                         .tableName(tableName)
                                                         .keySchema(KeySchemaElement.builder()
                                                                                    .attributeName("id")
                                                                                    .keyType(KeyType.HASH)
                                                                                    .build())
                                                         .attributeDefinitions(AttributeDefinition.builder()
                                                                                                  .attributeName("id")
                                                                                                  .attributeType(ScalarAttributeType.S)
                                                                                                  .build())
                                                         .billingMode(BillingMode.PAY_PER_REQUEST)
                                                         .build());

            dynamoDbClient.waiter().waitUntilTableExists(
                    DescribeTableRequest.builder().tableName(tableName).build()
            );
            System.out.println("✅ Created DynamoDB table: " + tableName);
        }
    }
}
