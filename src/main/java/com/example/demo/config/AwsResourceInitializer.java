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
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.ListIdentitiesRequest;
import software.amazon.awssdk.services.ses.model.ListIdentitiesResponse;
import software.amazon.awssdk.services.ses.model.VerifyEmailIdentityRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

@Component
public class AwsResourceInitializer {

    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final DynamoDbClient dynamoDbClient;
    private final SesClient sesClient;


    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.sqs.queue}")
    private String queueName;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    @Value("${aws.ses.sender}")
    private String senderEmail;

    public AwsResourceInitializer(S3Client s3Client, SqsClient sqsClient, DynamoDbClient dynamoDbClient, SesClient sesClient) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
        this.dynamoDbClient = dynamoDbClient;
        this.sesClient = sesClient;
    }

    @PostConstruct
    public void init() {
        // createS3Bucket();
        // createSqsQueue();
        // createDynamoTable();
        createSesEmailVerified();
    }

    private void createS3Bucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                                                 .bucket(bucketName)
                                                 .build());
            System.out.println("✅ S3 bucket already exists: " + bucketName);
        } catch (S3Exception e) {
            System.out.println("⚙️ Creating S3 bucket: " + bucketName);
            s3Client.createBucket(CreateBucketRequest.builder()
                                                     .bucket(bucketName)
                                                     .build());
            System.out.println("✅ Created S3 bucket: " + bucketName);
        }
    }

    private void createSqsQueue() {
        try {
            sqsClient.getQueueUrl(b -> b.queueName(queueName));
            System.out.println("✅ SQS queue already exists: " + queueName);
        } catch (QueueDoesNotExistException e) {
            System.out.println("⚙️ Creating SQS queue: " + queueName);
            sqsClient.createQueue(CreateQueueRequest.builder()
                                                    .queueName(queueName)
                                                    .build());
            System.out.println("✅ Created SQS queue: " + queueName);
        }
    }

    private void createDynamoTable() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                                                             .tableName(tableName)
                                                             .build());
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

            dynamoDbClient.waiter()
                          .waitUntilTableExists(
                                  DescribeTableRequest.builder()
                                                      .tableName(tableName)
                                                      .build()
                          );
            System.out.println("✅ Created DynamoDB table: " + tableName);
        }
    }

    private void createSesEmailVerified() {
        try {
            System.out.println("🔍 Checking existing SES identities...");

            ListIdentitiesResponse identitiesResponse = sesClient.listIdentities(ListIdentitiesRequest.builder()
                                                                                                      .build());

            boolean alreadyVerified = identitiesResponse.identities()
                                                        .stream()
                                                        .anyMatch(id -> id.equalsIgnoreCase(senderEmail));

            if (alreadyVerified) {
                System.out.println("✅ SES identity already exists: " + senderEmail);
            } else {
                System.out.println("⚙️ Verifying SES email identity: " + senderEmail);
                sesClient.verifyEmailIdentity(
                        VerifyEmailIdentityRequest.builder()
                                                  .emailAddress(senderEmail)
                                                  .build()
                );
                System.out.println("✅ SES email identity verified (LocalStack mock): " + senderEmail);
            }
        } catch (Exception e) {
            System.err.println("❌ Error initializing SES identity: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
