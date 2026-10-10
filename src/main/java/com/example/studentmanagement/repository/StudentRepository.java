package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    // 1. Tự động liên kết với @NamedQuery "Student.findByDepartmentId" được khai báo trong Entity Student
    List<Student> findByDepartmentId(@Param("departmentId") Long departmentId);

    // 2. Tự động liên kết với @NamedQuery "Student.findByNameContainingIgnoreCase" trong Entity Student
    List<Student> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    // 3. Tự động liên kết với @NamedQuery "Student.findByDobBetween" trong Entity Student
    List<Student> findByDobBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 4. Tự động liên kết với @NamedQuery "Student.findStudentsBornAfter" trong Entity Student
    List<Student> findStudentsBornAfter(@Param("dob") LocalDate dob);

    // 5. Tự động liên kết với @NamedQuery "Student.findAllWithDepartmentOrdered" trong Entity Student
    List<Student> findAllWithDepartmentOrdered();

    // 6. Tự động liên kết với @NamedNativeQuery "Student.findStudentsByDeptNameNative" trong Entity Student
    List<Student> findStudentsByDeptNameNative(@Param("deptName") String deptName);
}
