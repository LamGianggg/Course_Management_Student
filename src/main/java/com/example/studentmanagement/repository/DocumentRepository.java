package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByPath(String path);

    List<Document> findByAppId(String appId);

    List<Document> findByAppIdAndDeletedFalse(String appId);

    Optional<Document> findByIdAndDeletedFalse(String id);
}
