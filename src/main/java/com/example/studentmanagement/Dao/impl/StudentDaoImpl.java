package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IStudentDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.HibernateDao.StudentHibernateDao;
import com.example.studentmanagement.entity.Student;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class StudentDaoImpl extends HibernateGenericDao<Long, Student> implements IStudentDao {

    private static final Logger logger = LoggerFactory.getLogger(StudentDaoImpl.class);

    public static final Set<String> ALLOWED_SEARCH_FIELDS = StudentHibernateDao.ALLOWED_SEARCH_FIELDS;
    public static final Set<String> ALLOWED_SORT_FIELDS = StudentHibernateDao.ALLOWED_SORT_FIELDS;

    private final EntityManager entityManager;

    public StudentDaoImpl(EntityManager entityManager) {
        super(Student.class, entityManager);
        this.entityManager = entityManager;
        this.hibernateDao = new StudentHibernateDao(this);
    }

    @Override
    public Student save(Student student) {
        logger.info("Saving student: {}", student.getName());
        if (student.getId() == null || findByPk(student.getId()) == null) {
            return create(student);
        }
        return super.save(student);
    }

    @Override
    public Student update(Student student) {
        logger.info("Updating student id: {}", student.getId());
        return super.save(student);
    }

    @Override
    public Student findByPk(Long id) {
        if (id == null) {
            return null;
        }
        return entityManager.find(Student.class, id);
    }

    @Override
    public void delete(Long id) {
        logger.info("Deleting student id: {}", id);
        super.deleteById(id);
    }

    @Override
    public List<Student> getAll() {
        return super.findAll();
    }

    @Override
    public List<Student> findByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                        "SELECT s FROM Student s WHERE s.department.id = :deptId", Student.class)
                .setParameter("deptId", departmentId)
                .getResultList();
    }

    @Override
    public List<Student> findByNameContaining(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                        "SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :kw, '%'))", Student.class)
                .setParameter("kw", keyword.trim())
                .getResultList();
    }

    @Override
    public Page<Student> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }

    @Override
    public Page<Student> filter(Specification<Student> spec, Pageable pageable) {
        return super.findAll(spec != null ? spec : Specification.where(null), pageable);
    }

    @Override
    public Page<Student> filter(String search, int page, int size, List<String> sortParams) {
        return this.hibernateDao.find(search, page, size, sortParams);
    }

    @Override
    public HibernateDao<Student> getHibernateDao() {
        return this.hibernateDao;
    }
}
