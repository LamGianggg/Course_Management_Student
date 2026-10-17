package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.AppDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IAppDetailDao extends IGenericDao<String, AppDetail>, JpaSpecificationExecutor<AppDetail> {

    AppDetail findByPk(String id);

    AppDetail save(AppDetail appDetail);

    boolean existsById(String id);

    Page<AppDetail> findAll(Pageable pageable);

    Page<AppDetail> filter(Specification<AppDetail> spec, Pageable pageable);

    Page<AppDetail> filter(String search, int page, int size, List<String> sortParams);

    HibernateDao<AppDetail> getHibernateDao();
}
