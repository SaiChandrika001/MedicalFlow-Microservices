package com.medicalflow.reportservice.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadFile(MultipartFile file, Long patientId);

    Resource downloadFile(String key);

    String generatePresignedUrl(String key);

    void deleteFile(String key);

    String buildObjectUrl(String key);
}
