package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IDocumentDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.Document;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class DocumentDaoImpl extends HibernateGenericDao<String, Document> implements IDocumentDao {

    private static final Logger logger = LoggerFactory.getLogger(DocumentDaoImpl.class);

    private final EntityManager entityManager;

    public DocumentDaoImpl(EntityManager entityManager) {
        super(Document.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public Document save(Document document) {
        logger.info("Saving document: name={}, appId={}", document.getName(), document.getAppId());
        return create(document);
    }

    @Override
    public Document update(Document document) {
        logger.info("Updating document: id={}, name={}", document.getId(), document.getName());
        return super.save(document);
    }

    @Override
    public Document findByPk(String id) {
        if (id == null) {
            return null;
        }
        return entityManager.find(Document.class, id);
    }

    @Override
    public Document findByPath(String path) {
        if (path == null) {
            return null;
        }
        try {
            return entityManager.createQuery(
                            "SELECT d FROM Document d WHERE d.path = :path", Document.class)
                    .setParameter("path", path)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Document> findByAppId(String appId) {
        if (appId == null) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                        "SELECT d FROM Document d WHERE d.appId = :appId ORDER BY d.creationTime DESC", Document.class)
                .setParameter("appId", appId)
                .getResultList();
    }

    @Override
    public List<Document> findByAppIdAndDeletedFalse(String appId) {
        if (appId == null) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                        "SELECT d FROM Document d WHERE d.appId = :appId AND (d.deleted IS NULL OR d.deleted = false) ORDER BY d.creationTime DESC", Document.class)
                .setParameter("appId", appId)
                .getResultList();
    }

    @Override
    public List<Document> findAllActive() {
        return entityManager.createQuery(
                        "SELECT d FROM Document d WHERE (d.deleted IS NULL OR d.deleted = false) AND (d.active IS NULL OR d.active = true) ORDER BY d.creationTime DESC", Document.class)
                .getResultList();
    }

    @Override
    public void delete(String id) {
        logger.info("Hard deleting document id: {}", id);
        super.deleteById(id);
    }
}
