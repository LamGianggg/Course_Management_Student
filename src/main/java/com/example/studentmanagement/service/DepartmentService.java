package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.DepartmentRequest;
import com.example.studentmanagement.entity.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Department getDepartmentById(Long id);
    Department createDepartment(DepartmentRequest request);
    Department updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartment(Long id);
}
