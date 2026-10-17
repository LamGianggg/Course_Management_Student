package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.Dao.IDepartmentDao;
import com.example.studentmanagement.HibernateDao.DepartmentHibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.dto.DepartmentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private IDepartmentDao departmentDao;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Autowired(required = false)
    public void setDepartmentDao(IDepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    public Department createDepartment(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long id, DepartmentRequest request) {
        Department department = getDepartmentById(id);
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        departmentRepository.delete(department);
    }

    @Override
    public Page<Department> searchDepartments(Specification<Department> spec, Pageable pageable) {
        if (departmentDao != null) {
            return departmentDao.filter(spec, pageable);
        }
        return Page.empty(pageable);
    }

    @Override
    public Page<Department> filterDepartments(String search, int page, int size, List<String> sort) {
        if (departmentDao != null) {
            return departmentDao.filter(search, page, size, sort);
        }
        Pageable pageable = HibernateDao.createPageable(page, size, sort, DepartmentHibernateDao.ALLOWED_SORT_FIELDS);
        Specification<Department> spec = HibernateDao.parse(search, DepartmentHibernateDao.ALLOWED_SEARCH_FIELDS);
        return searchDepartments(spec, pageable);
    }
}
