package com.example.studentmanagement.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    @Value("${minio.access.name:educaze}")
    private String accessKey;

    @Value("${minio.access.secret:educaze2024}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }
}
