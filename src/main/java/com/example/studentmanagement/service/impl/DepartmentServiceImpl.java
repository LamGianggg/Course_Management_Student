package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.dto.DepartmentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
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
}
