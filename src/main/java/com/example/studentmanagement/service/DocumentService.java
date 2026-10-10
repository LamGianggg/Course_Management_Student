package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    List<Document> create(List<MultipartFile> files, String appId);

    ResponseEntity<byte[]> download(String filePath);

    String delete(Map<String, String> id);

    Document getById(String id);

    List<Document> getByAppId(String appId);

    List<Document> getAllActive();

    // Dynamic search, pagination, and sorting using HibernateDao
    Page<Document> filterDocuments(Specification<Document> spec, Pageable pageable);

    Page<Document> filterDocuments(String search, int page, int size, List<String> sortParams);
}
