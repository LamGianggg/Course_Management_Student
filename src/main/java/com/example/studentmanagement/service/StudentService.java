package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface StudentService {
    List<Student> getAllStudents();
    Student getStudentById(Long id);
    List<Student> getStudentsByDepartmentId(Long departmentId);
    List<Student> searchStudentsByName(String keyword);
    Student createStudent(StudentRequest request);
    Student updateStudent(Long id, StudentRequest request);
    void deleteStudent(Long id);

    // Các method gọi NamedQuery từ Entity
    List<Student> getStudentsByDobRange(java.time.LocalDate start, java.time.LocalDate end);
    List<Student> getStudentsBornAfter(java.time.LocalDate dob);
    List<Student> getAllWithDepartmentOrdered();
    List<Student> getStudentsByDeptNameNative(String deptName);

    // Phase 1+2+3: Specification-based search with pagination and sorting
    Page<Student> searchStudents(Specification<Student> spec, Pageable pageable);
    Page<Student> filterStudents(String search, int page, int size, List<String> sort);
}

