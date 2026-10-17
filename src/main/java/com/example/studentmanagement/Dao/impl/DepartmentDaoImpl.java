package com.example.studentmanagement.Dao.impl;

import com.example.studentmanagement.Dao.IDepartmentDao;
import com.example.studentmanagement.HibernateDao.DepartmentHibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.entity.Department;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class DepartmentDaoImpl extends HibernateGenericDao<Long, Department> implements IDepartmentDao {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentDaoImpl.class);

    public static final Set<String> ALLOWED_SEARCH_FIELDS = DepartmentHibernateDao.ALLOWED_SEARCH_FIELDS;
    public static final Set<String> ALLOWED_SORT_FIELDS = DepartmentHibernateDao.ALLOWED_SORT_FIELDS;

    private final EntityManager entityManager;

    public DepartmentDaoImpl(EntityManager entityManager) {
        super(Department.class, entityManager);
        this.entityManager = entityManager;
        this.hibernateDao = new DepartmentHibernateDao(this);
    }

    @Override
    public Department save(Department department) {
        logger.info("Saving department: {}", department.getName());
        if (department.getId() == null || findByPk(department.getId()) == null) {
            return create(department);
        }
        return super.save(department);
    }

    @Override
    public Department update(Department department) {
        logger.info("Updating department id: {}", department.getId());
        return super.save(department);
    }

    @Override
    public Department findByPk(Long id) {
        if (id == null) {
            return null;
        }
        return entityManager.find(Department.class, id);
    }

    @Override
    public void delete(Long id) {
        logger.info("Deleting department id: {}", id);
        super.deleteById(id);
    }

    @Override
    public List<Department> getAll() {
        return super.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return findByPk(id) != null;
    }

    @Override
    public Page<Department> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }

    @Override
    public Page<Department> filter(Specification<Department> spec, Pageable pageable) {
        return super.findAll(spec != null ? spec : Specification.where(null), pageable);
    }

    @Override
    public Page<Department> filter(String search, int page, int size, List<String> sortParams) {
        return this.hibernateDao.find(search, page, size, sortParams);
    }

    @Override
    public HibernateDao<Department> getHibernateDao() {
        return this.hibernateDao;
    }
}
