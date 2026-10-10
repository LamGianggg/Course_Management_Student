package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IDepartmentDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.Department;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentDaoImpl extends HibernateGenericDao<Long, Department> implements IDepartmentDao {

    public DepartmentDaoImpl(EntityManager entityManager) {
        super(Department.class, entityManager);
    }
}
