package com.example.studentmanagement.HibernateDao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IGenericDao<Pk, Entity> {

    @Transactional
    Entity create(Entity anEntity);

    Class<Entity> getType();

    Page<Entity> filter(String search, int page, int size, List<String> sortParams);

    Page<Entity> filter(Specification<Entity> spec, Pageable pageable);

    HibernateDao<Entity> getHibernateDao();
}
