package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.constants.CoreConstants;
import com.example.studentmanagement.exception.BusinessException;
import com.example.studentmanagement.service.MinioService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioServiceImpl implements MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioServiceImpl.class);

    private final MinioClient minioClient;

    @Autowired
    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void ensureBucketExists(String bucket) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                logger.info("MinIO bucket '{}' does not exist, creating it...", bucket);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            logger.warn("Could not check/create MinIO bucket '{}': {}", bucket, e.getMessage());
        }
    }

    @Override
    public void uploadFile(String bucket, String objectPath, InputStream inputStream, long size, String contentType) {
        try {
            ensureBucketExists(bucket);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .stream(inputStream, size, -1)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build()
            );
            logger.info("MinIO upload success: bucket={}, objectPath={}", bucket, objectPath);
        } catch (Exception e) {
            logger.error("MinIO upload failed for objectPath={}: {}", objectPath, e.getMessage());
            throw new BusinessException(CoreConstants.MINIO_UPLOAD_FAILED, e);
        }
    }

    @Override
    public byte[] downloadFile(String bucket, String objectPath) {
        try (GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectPath)
                        .build())) {
            return response.readAllBytes();
        } catch (ErrorResponseException e) {
            logger.error("MinIO error for objectPath={}: {}", objectPath, e.getMessage());
            if (e.errorResponse() != null && "NoSuchKey".equalsIgnoreCase(e.errorResponse().code())) {
                throw new BusinessException(CoreConstants.FILE_NOT_FOUND, e);
            }
            throw new BusinessException(CoreConstants.MINIO_DOWNLOAD_FAILED, e);
        } catch (Exception e) {
            logger.error("MinIO download failed for objectPath={}: {}", objectPath, e.getMessage());
            throw new BusinessException(CoreConstants.MINIO_DOWNLOAD_FAILED, e);
        }
    }

    @Override
    public void deleteFile(String bucket, String objectPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .build()
            );
            logger.info("MinIO object deleted: bucket={}, objectPath={}", bucket, objectPath);
        } catch (Exception e) {
            logger.error("MinIO delete failed for objectPath={}: {}", objectPath, e.getMessage());
            throw new BusinessException(CoreConstants.MINIO_DELETE_FAILED, e);
        }
    }

    @Override
    public boolean doesObjectExist(String bucket, String objectPath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectPath)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
