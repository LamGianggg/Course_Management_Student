package com.example.studentmanagement.Dao;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.IGenericDao;
import com.example.studentmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IStudentDao extends IGenericDao<Long, Student>, JpaSpecificationExecutor<Student> {

    Student save(Student student);

    Student update(Student student);

    Student findByPk(Long id);

    void delete(Long id);

    List<Student> getAll();

    List<Student> findByDepartmentId(Long departmentId);

    List<Student> findByNameContaining(String keyword);

    Page<Student> findAll(Pageable pageable);

    Page<Student> filter(Specification<Student> spec, Pageable pageable);

    Page<Student> filter(String search, int page, int size, List<String> sortParams);

    HibernateDao<Student> getHibernateDao();
}
