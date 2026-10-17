package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IDepartmentDao extends IGenericDao<Long, Department>, JpaSpecificationExecutor<Department> {

    Department save(Department department);

    Department update(Department department);

    Department findByPk(Long id);

    void delete(Long id);

    List<Department> getAll();

    boolean existsById(Long id);

    Page<Department> findAll(Pageable pageable);

    Page<Department> filter(Specification<Department> spec, Pageable pageable);

    Page<Department> filter(String search, int page, int size, List<String> sortParams);

    HibernateDao<Department> getHibernateDao();
}
