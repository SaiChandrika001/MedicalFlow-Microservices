package com.medicalflow.reportservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
public class AwsS3Config {

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(AwsS3Properties awsS3Properties) {
        if (StringUtils.hasText(awsS3Properties.getAccessKey()) && StringUtils.hasText(awsS3Properties.getSecretKey())) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            awsS3Properties.getAccessKey(),
                            awsS3Properties.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider awsCredentialsProvider, AwsS3Properties awsS3Properties) {
        return S3Client.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider awsCredentialsProvider, AwsS3Properties awsS3Properties) {
        return S3Presigner.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
