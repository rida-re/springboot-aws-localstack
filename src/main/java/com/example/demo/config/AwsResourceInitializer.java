package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class AwsResourceInitializer {

    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final DynamoDbClient dynamoDbClient;
    private final SesClient sesClient;
    private final RdsClient rdsClient;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.sqs.queue}")
    private String queueName;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    @Value("${aws.ses.sender}")
    private String senderEmail;

    @Value("${aws.rds.instance}")
    private String rdsInstanceId;

    @Value("${aws.rds.dbName}")
    private String rdsDbName;

    @Value("${aws.rds.username}")
    private String rdsUsername;

    @Value("${aws.rds.password}")
    private String rdsPassword;

    @Autowired
    private LambdaClient lambdaClient;

    @Value("${aws.lambda.functionName:demo-function}")
    private String functionName;

    @Value("${aws.lambda.handler:com.example.demo.LambdaHandler::handleRequest}")
    private String handler;

    @Value("${aws.lambda.runtime:java11}")
    private String runtime;


    public AwsResourceInitializer(S3Client s3Client, SqsClient sqsClient, DynamoDbClient dynamoDbClient, SesClient sesClient, RdsClient rdsClient) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
        this.dynamoDbClient = dynamoDbClient;
        this.sesClient = sesClient;
        this.rdsClient = rdsClient;
    }

    @PostConstruct
    public void init() {
        // createS3Bucket();
        // createSqsQueue();
        // createDynamoTable();
        // createRdsInstance();
        // createSesEmailVerified();
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

    private void createRdsInstance() {
        try {
            DescribeDbInstancesResponse resp = rdsClient.describeDBInstances(DescribeDbInstancesRequest.builder()
                                                                                                   .dbInstanceIdentifier(rdsInstanceId)
                                                                                                   .build());
            System.out.println("✅ RDS instance already exists: " + rdsInstanceId);
        } catch (RdsException e) {
            System.out.println("⚙️ Creating RDS Postgres instance: " + rdsInstanceId);
            rdsClient.createDBInstance(CreateDbInstanceRequest.builder()
                                                              .dbInstanceIdentifier(rdsInstanceId)
                                                              .engine("postgres")
                                                              .engineVersion("12")
                                                              .dbInstanceClass("db.t3.micro")
                                                              .allocatedStorage(20)
                                                              .masterUsername(rdsUsername)
                                                              .masterUserPassword(rdsPassword)
                                                              .dbName(rdsDbName)
                                                              .build());
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                    DescribeDbInstancesResponse check = rdsClient.describeDBInstances(DescribeDbInstancesRequest.builder()
                                                                                                          .dbInstanceIdentifier(rdsInstanceId)
                                                                                                          .build());
                    DBInstance db = check.dbInstances().get(0);
                    String status = db.dbInstanceStatus();
                    System.out.println("⏳ RDS status: " + status);
                    if ("available".equalsIgnoreCase(status)) {
                        System.out.println("✅ RDS instance available: " + rdsInstanceId);
                        break;
                    }
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void createLambdaFunction() {
        try {
            // Check if function exists
            GetFunctionResponse existing = lambdaClient.getFunction(
                    GetFunctionRequest.builder().functionName(functionName).build()
            );
            System.out.println("Lambda function already exists: " + existing.configuration().functionName());
        } catch (LambdaException e) {
            // Create Lambda function
            CreateFunctionRequest request = null;
            try {
                request = CreateFunctionRequest.builder()
                                               .functionName(functionName)
                                               .runtime(runtime)
                                               .role("arn:aws:iam::000000000000:role/lambda-ex") // dummy role for LocalStack
                                               .handler(handler)
                                               .code(FunctionCode.builder()
                                                                 .zipFile(SdkBytes.fromByteArray(Files.readAllBytes(Paths.get("src/main/resources/lambda/demo-function.zip"))))
                                                                 .build())
                                               .build();
            } catch (IOException ex) {
                System.out.println("Lambda error : " + ex);
            }
            lambdaClient.createFunction(request);
            System.out.println("Lambda function created: " + functionName);
        }
    }
}
