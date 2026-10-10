package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository, DepartmentRepository departmentRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    public List<Student> getStudentsByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        return studentRepository.findByDepartmentId(departmentId);
    }

    @Override
    public List<Student> searchStudentsByName(String keyword) {
        return studentRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public Student createStudent(StudentRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Student student = new Student();
        student.setName(request.getName());
        student.setDob(request.getDob());
        student.setDepartment(department);

        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(Long id, StudentRequest request) {
        Student student = getStudentById(id);
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        student.setName(request.getName());
        student.setDob(request.getDob());
        student.setDepartment(department);

        return studentRepository.save(student);
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    @Override
    public List<Student> getStudentsByDobRange(java.time.LocalDate start, java.time.LocalDate end) {
        return studentRepository.findByDobBetween(start, end);
    }

    @Override
    public List<Student> getStudentsBornAfter(java.time.LocalDate dob) {
        return studentRepository.findStudentsBornAfter(dob);
    }

    @Override
    public List<Student> getAllWithDepartmentOrdered() {
        return studentRepository.findAllWithDepartmentOrdered();
    }

    @Override
    public List<Student> getStudentsByDeptNameNative(String deptName) {
        return studentRepository.findStudentsByDeptNameNative(deptName);
    }

    @Override
    public Page<Student> searchStudents(Specification<Student> spec, Pageable pageable) {
        return studentRepository.findAll(spec != null ? spec : Specification.where(null), pageable);
    }
}
