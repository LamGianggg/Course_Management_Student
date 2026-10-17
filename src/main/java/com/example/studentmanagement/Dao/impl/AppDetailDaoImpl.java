package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IAppDetailDao;
import com.example.studentmanagement.HibernateDao.AppDetailHibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.AppDetail;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class AppDetailDaoImpl extends HibernateGenericDao<String, AppDetail> implements IAppDetailDao {

    public static final Set<String> ALLOWED_SEARCH_FIELDS = AppDetailHibernateDao.ALLOWED_SEARCH_FIELDS;
    public static final Set<String> ALLOWED_SORT_FIELDS = AppDetailHibernateDao.ALLOWED_SORT_FIELDS;

    private final EntityManager entityManager;

    public AppDetailDaoImpl(EntityManager entityManager) {
        super(AppDetail.class, entityManager);
        this.entityManager = entityManager;
        this.hibernateDao = new AppDetailHibernateDao(this);
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

    @Override
    public Page<AppDetail> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }

    @Override
    public Page<AppDetail> filter(Specification<AppDetail> spec, Pageable pageable) {
        return super.findAll(spec != null ? spec : Specification.where(null), pageable);
    }

    @Override
    public Page<AppDetail> filter(String search, int page, int size, List<String> sortParams) {
        return this.hibernateDao.find(search, page, size, sortParams);
    }

    @Override
    public HibernateDao<AppDetail> getHibernateDao() {
        return this.hibernateDao;
    }
}
