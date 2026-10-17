package com.example.studentmanagement.HibernateDao;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class HibernateGenericDao<Pk, Entity>
        extends SimpleJpaRepository<Entity, Pk>
        implements IGenericDao<Pk, Entity> {

    private static final Logger logger =
            LoggerFactory.getLogger(HibernateGenericDao.class);

    private Class<Entity> type;
    private EntityManager entityManager;
    protected HibernateDao<Entity> hibernateDao;

    public HibernateGenericDao(Class<Entity> type,
                               EntityManager entityManager) {
        super(type, entityManager);
        this.type = type;
        this.entityManager = entityManager;
        this.hibernateDao = new HibernateDao<>(this);
    }

    public HibernateGenericDao(Class<Entity> type,
                               EntityManager entityManager,
                               HibernateDao<Entity> hibernateDao) {
        super(type, entityManager);
        this.type = type;
        this.entityManager = entityManager;
        this.hibernateDao = hibernateDao != null ? hibernateDao : new HibernateDao<>(this);
        if (this.hibernateDao.getRepository() == null) {
            this.hibernateDao.setRepository(this);
        }
    }

    @Override
    @Transactional
    public Entity create(Entity anEntity) {
        if (anEntity == null) {
            logger.warn("Cannot create a null entity.");
            throw new IllegalArgumentException(
                    "Cannot create null entity[" + getType().getName() + "]"
            );
        } else {
            entityManager.persist(anEntity);
            return anEntity;
        }
    }

    @Override
    public Page<Entity> filter(String search, int page, int size, List<String> sortParams) {
        if (this.hibernateDao != null) {
            return this.hibernateDao.find(search, page, size, sortParams);
        }
        return findAll(HibernateDao.createPageable(page, size));
    }

    @Override
    public Page<Entity> filter(Specification<Entity> spec, Pageable pageable) {
        return findAll(spec != null ? spec : Specification.where(null), pageable);
    }

    @Override
    public HibernateDao<Entity> getHibernateDao() {
        return this.hibernateDao;
    }

    public void setHibernateDao(HibernateDao<Entity> hibernateDao) {
        this.hibernateDao = hibernateDao;
        if (this.hibernateDao != null && this.hibernateDao.getRepository() == null) {
            this.hibernateDao.setRepository(this);
        }
    }

    public Class<Entity> getType() {
        return type;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}