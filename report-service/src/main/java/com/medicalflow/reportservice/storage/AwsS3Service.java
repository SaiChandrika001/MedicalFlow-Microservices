package com.medicalflow.reportservice.storage;

import com.medicalflow.reportservice.config.AwsS3Properties;
import com.medicalflow.reportservice.exception.FileStorageException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
public class AwsS3Service implements S3Service {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsS3Properties awsS3Properties;

    public AwsS3Service(S3Client s3Client, S3Presigner s3Presigner, AwsS3Properties awsS3Properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.awsS3Properties = awsS3Properties;
    }

    @Override
    public String uploadFile(MultipartFile file, Long patientId) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("A valid report file is required.");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileStorageException("Unsupported file type. Upload only PDF or image files.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFileName);
        }

        String key = buildStorageKey(patientId, originalFileName);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsS3Properties.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PRIVATE)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException ex) {
            throw new FileStorageException("Failed to read file data for upload.", ex);
        } catch (S3Exception ex) {
            throw new FileStorageException("Failed to upload file to AWS S3: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public Resource downloadFile(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(awsS3Properties.getBucketName())
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return new InputStreamResource(s3Object);
        } catch (S3Exception ex) {
            throw new FileStorageException("Failed to download file from AWS S3: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public String generatePresignedUrl(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(awsS3Properties.getBucketName())
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(awsS3Properties.getPresignedUrlExpirationMinutes()))
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (S3Exception ex) {
            throw new FileStorageException("Unable to generate presigned download URL: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(awsS3Properties.getBucketName())
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception ex) {
            throw new FileStorageException("Failed to delete file from AWS S3: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public String buildObjectUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                awsS3Properties.getBucketName(),
                awsS3Properties.getRegion(),
                key);
    }

    private String buildStorageKey(Long patientId, String originalFileName) {
        String fileName = originalFileName.replaceAll("\\s+", "_");
        String uniqueId = UUID.randomUUID().toString();
        return String.format("reports/%d/%s-%s", patientId, uniqueId, fileName);
    }
}
