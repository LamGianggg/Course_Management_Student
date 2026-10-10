package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IStudentDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.Student;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class StudentDaoImpl extends HibernateGenericDao<Long, Student> implements IStudentDao {

    public StudentDaoImpl(EntityManager entityManager) {
        super(Student.class, entityManager);
    }
}
