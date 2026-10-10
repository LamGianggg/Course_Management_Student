package com.example.studentmanagement.HibernateDao;

import org.springframework.transaction.annotation.Transactional;

public interface IGenericDao<Pk, Entity> {

    @Transactional
    Entity create(Entity anEntity);

    Class<Entity> getType();
}
