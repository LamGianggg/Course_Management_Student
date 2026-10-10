package com.example.studentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NamedQueries({
    @NamedQuery(
        name = "Student.findByDepartmentId",
        query = "SELECT s FROM Student s WHERE s.department.id = :departmentId"
    ),
    @NamedQuery(
        name = "Student.findByNameContainingIgnoreCase",
        query = "SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))"
    ),
    @NamedQuery(
        name = "Student.findByDobBetween",
        query = "SELECT s FROM Student s WHERE s.dob BETWEEN :startDate AND :endDate ORDER BY s.dob ASC"
    ),
    @NamedQuery(
        name = "Student.findStudentsBornAfter",
        query = "SELECT s FROM Student s WHERE s.dob >= :dob ORDER BY s.dob DESC"
    ),
    @NamedQuery(
        name = "Student.findAllWithDepartmentOrdered",
        query = "SELECT s FROM Student s JOIN FETCH s.department d ORDER BY d.name ASC, s.name ASC"
    )
})
@NamedNativeQueries({
    @NamedNativeQuery(
        name = "Student.findStudentsByDeptNameNative",
        query = "SELECT s.* FROM students s INNER JOIN departments d ON s.department_id = d.id WHERE d.name = :deptName",
        resultClass = Student.class
    )
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "dob", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public Student() {
    }

    public Student(Long id, String name, LocalDate dob, Department department) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
