package com.medicalflow.reportservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

    private String region;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private int presignedUrlExpirationMinutes = 15;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getPresignedUrlExpirationMinutes() {
        return presignedUrlExpirationMinutes;
    }

    public void setPresignedUrlExpirationMinutes(int presignedUrlExpirationMinutes) {
        this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
    }
}
