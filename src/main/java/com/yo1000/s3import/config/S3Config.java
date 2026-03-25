package com.yo1000.s3import.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
public class S3Config {
    @Bean
    public S3Client s3Client(S3ClientBuilder builder, S3Properties props) {
        if (props.getEndpoint() != null && !props.getEndpoint().trim().isEmpty()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        return builder
                .forcePathStyle(props.isPathStyle())
                .build();
    }
}
