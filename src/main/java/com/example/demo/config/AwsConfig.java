package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class AwsConfig {

    private static final String LOCALSTACK_ENDPOINT =
            System.getenv().getOrDefault("LOCALSTACK_ENDPOINT", "http://localhost:4566");

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                       .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                       .region(Region.US_EAST_1)
                       .credentialsProvider(
                               StaticCredentialsProvider.create(
                                       AwsBasicCredentials.create("test", "test")
                               )
                       )
                       .serviceConfiguration(
                        S3Configuration.builder()
                                       .pathStyleAccessEnabled(true) // 👈 important!
                                       .build())
                       .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                        .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create("test", "test")
                                )
                        )
                        .build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                             .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                             .region(Region.US_EAST_1)
                             .credentialsProvider(
                                     StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"))
                             )
                             .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                        .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create("test", "test")
                                )
                        )
                        .build();
    }
}
