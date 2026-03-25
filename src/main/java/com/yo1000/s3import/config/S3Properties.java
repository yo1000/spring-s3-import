package com.yo1000.s3import.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {
    private boolean pathStyle = false;
    private String endpoint;
    private String bucketName;

    public boolean isPathStyle() {
        return pathStyle;
    }

    public void setPathStyle(boolean pathStyle) {
        this.pathStyle = pathStyle;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
