package com.example.studentmanagement;

import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class StudentManagementApplicationUnitTest {

    @Test
    void testMainMethod() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            StudentManagementApplication.main(new String[0]);
            mocked.verify(() -> SpringApplication.run(StudentManagementApplication.class, new String[0]));
        }
    }

    @Test
    void testRunTestQueries() throws Exception {
        StudentManagementApplication app = new StudentManagementApplication();
        StudentRepository mockRepo = mock(StudentRepository.class);

        Department dept = new Department(1L, "IT", "Information Tech");
        Student student = new Student(1L, "Alice", LocalDate.of(2003, 5, 20), dept);

        when(mockRepo.findByDobBetween(any(), any())).thenReturn(List.of(student));
        when(mockRepo.findStudentsBornAfter(any())).thenReturn(List.of(student));
        when(mockRepo.findAllWithDepartmentOrdered()).thenReturn(List.of(student));
        when(mockRepo.findStudentsByDeptNameNative(anyString())).thenReturn(List.of(student));

        CommandLineRunner runner = app.runTestQueries(mockRepo);
        assertNotNull(runner);
        runner.run(new String[0]);

        verify(mockRepo).findByDobBetween(any(), any());
        verify(mockRepo).findStudentsBornAfter(any());
        verify(mockRepo).findAllWithDepartmentOrdered();
        verify(mockRepo).findStudentsByDeptNameNative("Information Technology");
    }
}
