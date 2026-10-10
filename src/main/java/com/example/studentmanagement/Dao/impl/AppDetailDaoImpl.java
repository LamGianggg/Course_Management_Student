package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IAppDetailDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.AppDetail;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class AppDetailDaoImpl extends HibernateGenericDao<String, AppDetail> implements IAppDetailDao {

    private final EntityManager entityManager;

    public AppDetailDaoImpl(EntityManager entityManager) {
        super(AppDetail.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public AppDetail findByPk(String id) {
        if (id == null) {
            return null;
        }
        return entityManager.find(AppDetail.class, id);
    }

    @Override
    public AppDetail save(AppDetail appDetail) {
        if (appDetail.getId() == null || findByPk(appDetail.getId()) == null) {
            return create(appDetail);
        }
        return super.save(appDetail);
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return findByPk(id) != null;
    }
}
