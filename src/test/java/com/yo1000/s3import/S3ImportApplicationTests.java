package com.yo1000.s3import;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@SpringBootTest
@Testcontainers
class S3ImportApplicationTests {
    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer("localstack/localstack:4.12")
                    .withServices("s3");

    static final boolean S3_PATH_STYLE = true;
    static final String S3_BUCKET_NAME = "test";
    static final String S3_FILE_PATH = "test_users.csv.gz";

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        System.setProperty("aws.region", localStack.getRegion());
        System.setProperty("aws.accessKeyId", localStack.getAccessKey());
        System.setProperty("aws.secretAccessKey", localStack.getSecretKey());

        registry.add("app.s3.path-style", () -> S3_PATH_STYLE);
        registry.add("app.s3.endpoint", () -> localStack.getEndpoint());
        registry.add("app.s3.bucket-name", () -> S3_BUCKET_NAME);
        registry.add("app.csv.file-path", () -> S3_FILE_PATH);
    }

    @BeforeAll
    static void setup() throws IOException {
        try (S3Client s3Client = S3Client.builder()
                .forcePathStyle(S3_PATH_STYLE)
                .endpointOverride(localStack.getEndpoint())
                .region(Region.of(localStack.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        localStack.getAccessKey(),
                        localStack.getSecretKey())))
                .build()) {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(S3_BUCKET_NAME)
                    .build());

            ClassPathResource employeeResource = new ClassPathResource(S3_FILE_PATH);
            try (InputStream inputStream = employeeResource.getInputStream()) {
                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(S3_BUCKET_NAME)
                                .key(S3_FILE_PATH)
                                .contentType("application/gzip")
                                .build(),
                        RequestBody
                                .fromInputStream(inputStream, employeeResource.contentLength()));
            }
        }
    }

    @Test
	void contextLoads() {}
}
