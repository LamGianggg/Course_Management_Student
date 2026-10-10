package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IDocumentDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.Document;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class DocumentDaoImpl extends HibernateGenericDao<String, Document> implements IDocumentDao {

    private static final Logger logger = LoggerFactory.getLogger(DocumentDaoImpl.class);

    public static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "appId", "owner", "path", "version", "deleted", "active", "creationTime"
    );

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "appId", "owner", "version", "creationTime", "modificationTime", "active", "deleted"
    );

    private final EntityManager entityManager;
    private final HibernateDao<Document> hibernateDao;

    public DocumentDaoImpl(EntityManager entityManager) {
        super(Document.class, entityManager);
        this.entityManager = entityManager;
        this.hibernateDao = new HibernateDao<>(
                this,
                ALLOWED_SEARCH_FIELDS,
                ALLOWED_SORT_FIELDS,
                "creationTime",
                Sort.Direction.DESC
        );
    }

    @Override
    public Document save(Document document) {
        logger.info("Saving document: name={}, appId={}", document.getName(), document.getAppId());
        if (document.getId() == null || findByPk(document.getId()) == null) {
            return create(document);
        }
        return super.save(document);
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

    @Override
    public Page<Document> findAll(Pageable pageable) {
        logger.info("Finding all documents with pageable");
        return super.findAll(pageable);
    }

    @Override
    public Page<Document> filter(Specification<Document> spec, Pageable pageable) {
        logger.info("Filtering documents by specification and pageable");
        return findAll(spec != null ? spec : Specification.where(null), pageable);
    }

    @Override
    public Page<Document> filter(String search, int page, int size, List<String> sortParams) {
        logger.info("Filtering documents by search expression: {}, page: {}, size: {}", search, page, size);
        return hibernateDao.find(search, page, size, sortParams);
    }

    @Override
    public HibernateDao<Document> getHibernateDao() {
        return this.hibernateDao;
    }
}
