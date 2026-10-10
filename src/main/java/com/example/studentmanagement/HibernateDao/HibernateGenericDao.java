package com.example.studentmanagement.HibernateDao;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class HibernateGenericDao<Pk, Entity>
        extends SimpleJpaRepository<Entity, Pk>
        implements IGenericDao<Pk, Entity> {

    private static final Logger logger =
            LoggerFactory.getLogger(HibernateGenericDao.class);

    private Class<Entity> type;
    private EntityManager entityManager;

    public HibernateGenericDao(Class<Entity> type,
                               EntityManager entityManager) {
        super(type, entityManager);
        this.type = type;
        this.entityManager = entityManager;
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
            return save(anEntity);
        }
    }

    public Class<Entity> getType() {
        return type;
    }
}