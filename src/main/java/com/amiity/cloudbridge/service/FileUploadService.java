package com.amiity.cloudbridge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Slf4j
@Service
public class FileUploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public FileUploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) {

        try {
            String key = "uploads/" + file.getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            log.info("Uploading File....");
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return "File uploaded successfully: " + key;

        } catch (IOException e) {
            return "Upload failed: " + e.getMessage();
        }
    }
}
