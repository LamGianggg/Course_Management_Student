package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IClassInfoDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.ClassInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class ClassInfoDaoImpl extends HibernateGenericDao<String, ClassInfo> implements IClassInfoDao {

    private static final Logger logger = LoggerFactory.getLogger(ClassInfoDaoImpl.class);

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("name", "domain", "createdTime", "id");

    private final EntityManager entityManager;

    public ClassInfoDaoImpl(final EntityManager entityManager) {
        super(ClassInfo.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public ClassInfo save(ClassInfo classInfo) {
        logger.info("Saving ClassInfo: {}", classInfo.getName());
        return create(classInfo);
    }

    @Override
    public ClassInfo update(ClassInfo classInfo) {
        logger.info("Updating ClassInfo: {}", classInfo.getId());
        return super.save(classInfo);
    }

    @Override
    public void delete(String id) {
        logger.info("Deleting ClassInfo: {}", id);
        super.deleteById(id);
    }

    @Override
    public ClassInfo getById(String id) {
        logger.info("Getting ClassInfo by id: {}", id);
        if (id == null) {
            return null;
        }
        return entityManager.find(ClassInfo.class, id);
    }

    @Override
    public ClassInfo getByName(String name) {
        logger.info("Getting ClassInfo by name: {}", name);
        try {
            TypedQuery<ClassInfo> query = entityManager
                    .createQuery("SELECT c FROM ClassInfo c WHERE c.name = :name", ClassInfo.class)
                    .setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<ClassInfo> getAll() {
        logger.info("Getting all ClassInfo");
        try {
            TypedQuery<ClassInfo> query = entityManager
                    .createQuery("SELECT c FROM ClassInfo c ORDER BY c.createdTime DESC", ClassInfo.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.warn("Get all ClassInfo failed, returning empty list", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ClassInfo> getByDomain(String domain) {
        logger.info("Getting ClassInfo by domain: {}", domain);
        try {
            TypedQuery<ClassInfo> query = entityManager
                    .createQuery("SELECT c FROM ClassInfo c WHERE c.domain = :domain ORDER BY c.createdTime DESC", ClassInfo.class)
                    .setParameter("domain", domain);
            return query.getResultList();
        } catch (Exception e) {
            logger.warn("Get ClassInfo by domain failed, returning empty list", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ClassInfo> search(String query, Integer lowerLimit, Integer upperLimit, String orderBy, String orderType) {
        logger.info("Searching ClassInfo with query: {}", query);

        String safeOrderBy = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "createdTime";
        String safeOrderType = "ASC".equalsIgnoreCase(orderType) ? "ASC" : "DESC";

        boolean hasQuery = query != null && !query.trim().isEmpty();
        String jpql = "SELECT c FROM ClassInfo c "
                + (hasQuery ? "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) "
                        + "OR LOWER(c.domain) LIKE LOWER(CONCAT('%', :query, '%')) " : "")
                + "ORDER BY c." + safeOrderBy + " " + safeOrderType;

        try {
            TypedQuery<ClassInfo> typedQuery = entityManager.createQuery(jpql, ClassInfo.class);
            if (hasQuery) {
                typedQuery.setParameter("query", query.trim());
            }
            // lowerLimit = offset (firstResult), upperLimit = end position -> maxResults = upper - lower
            if (lowerLimit != null && lowerLimit >= 0) {
                typedQuery.setFirstResult(lowerLimit);
            }
            if (upperLimit != null && upperLimit > 0) {
                int first = (lowerLimit != null && lowerLimit >= 0) ? lowerLimit : 0;
                int maxResults = upperLimit > first ? upperLimit - first : upperLimit;
                typedQuery.setMaxResults(maxResults);
            }
            return typedQuery.getResultList();
        } catch (Exception e) {
            logger.warn("Search ClassInfo failed, returning empty list", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Integer getCount(String query) {
        logger.info("Getting ClassInfo count with query: {}", query);
        try {
            boolean hasQuery = query != null && !query.trim().isEmpty();
            String jpql = "SELECT COUNT(c) FROM ClassInfo c "
                    + (hasQuery ? "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) "
                            + "OR LOWER(c.domain) LIKE LOWER(CONCAT('%', :query, '%'))" : "");
            TypedQuery<Long> countQuery = entityManager.createQuery(jpql, Long.class);
            if (hasQuery) {
                countQuery.setParameter("query", query.trim());
            }
            return countQuery.getSingleResult().intValue();
        } catch (Exception e) {
            logger.warn("Get ClassInfo count failed, returning 0", e);
            return 0;
        }
    }
}
