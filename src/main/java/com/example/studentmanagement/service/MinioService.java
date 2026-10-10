package com.example.studentmanagement.service;

import java.io.InputStream;

public interface MinioService {

    void uploadFile(String bucket, String objectPath, InputStream inputStream, long size, String contentType);

    byte[] downloadFile(String bucket, String objectPath);

    void deleteFile(String bucket, String objectPath);

    boolean doesObjectExist(String bucket, String objectPath);

    void ensureBucketExists(String bucket);
}
