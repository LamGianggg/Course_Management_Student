package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.ClassInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IClassInfoDao extends IGenericDao<String, ClassInfo>, JpaSpecificationExecutor<ClassInfo> {

    ClassInfo save(ClassInfo classInfo);

    ClassInfo update(ClassInfo classInfo);

    void delete(String id);

    ClassInfo getById(String id);

    ClassInfo getByName(String name);

    List<ClassInfo> getAll();

    List<ClassInfo> getByDomain(String domain);

    List<ClassInfo> search(String query, Integer lowerLimit, Integer upperLimit, String orderBy, String orderType);

    Integer getCount(String query);

    Page<ClassInfo> findAll(Pageable pageable);

    Page<ClassInfo> filter(Specification<ClassInfo> spec, Pageable pageable);

    Page<ClassInfo> filter(String search, int page, int size, List<String> sortParams);

    HibernateDao<ClassInfo> getHibernateDao();
}
