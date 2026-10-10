package com.example.studentmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class StudentRequest {

    @NotBlank(message = "Student name is required")
    @Size(min = 2, max = 100, message = "Student name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Date of birth (dob) is required")
    @Past(message = "Date of birth must be a past date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    public StudentRequest() {
    }

    public StudentRequest(String name, LocalDate dob, Long departmentId) {
        this.name = name;
        this.dob = dob;
        this.departmentId = departmentId;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
