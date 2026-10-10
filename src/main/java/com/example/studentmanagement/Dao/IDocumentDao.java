package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.Document;

import java.util.List;

public interface IDocumentDao extends IGenericDao<String, Document> {

    Document save(Document document);

    Document update(Document document);

    Document findByPk(String id);

    Document findByPath(String path);

    List<Document> findByAppId(String appId);

    List<Document> findByAppIdAndDeletedFalse(String appId);

    List<Document> findAllActive();

    void delete(String id);
}
