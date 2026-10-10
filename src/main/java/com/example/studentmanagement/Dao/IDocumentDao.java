package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IDocumentDao extends IGenericDao<String, Document>, JpaSpecificationExecutor<Document> {

    Document save(Document document);

    Document update(Document document);

    Document findByPk(String id);

    Document findByPath(String path);

    List<Document> findByAppId(String appId);

    List<Document> findByAppIdAndDeletedFalse(String appId);

    List<Document> findAllActive();

    void delete(String id);

    // HibernateDao Integration methods
    Page<Document> findAll(Pageable pageable);

    Page<Document> filter(Specification<Document> spec, Pageable pageable);

    Page<Document> filter(String search, int page, int size, List<String> sortParams);

    HibernateDao<Document> getHibernateDao();
}
