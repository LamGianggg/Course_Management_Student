package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IStudentDao;
import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceBranchTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private IStudentDao studentDao;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void testSearchAndFilterWithStudentDaoNull() {
        StudentServiceImpl service = new StudentServiceImpl(studentRepository, departmentRepository);
        // studentDao is null
        Pageable pageable = PageRequest.of(0, 10);
        Student s = new Student(1L, "Alice", LocalDate.of(2003, 1, 1), null);
        Page<Student> expected = new PageImpl<>(List.of(s));

        when(studentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        // spec != null
        Page<Student> res1 = service.searchStudents((root, q, cb) -> cb.conjunction(), pageable);
        assertEquals(1, res1.getTotalElements());

        // spec == null
        Page<Student> res2 = service.searchStudents(null, pageable);
        assertEquals(1, res2.getTotalElements());

        // filterStudents
        Page<Student> res3 = service.filterStudents("name:Alice", 0, 10, List.of("name,asc"));
        assertEquals(1, res3.getTotalElements());
    }

    @Test
    void testSearchAndFilterWithStudentDaoNotNull() {
        studentService.setStudentDao(studentDao);
        Student s = new Student(1L, "Bob", LocalDate.of(2002, 2, 2), null);
        Page<Student> expected = new PageImpl<>(List.of(s));

        when(studentDao.filter(any(Specification.class), any(Pageable.class))).thenReturn(expected);
        Page<Student> res1 = studentService.searchStudents((root, q, cb) -> cb.conjunction(), PageRequest.of(0, 10));
        assertEquals(1, res1.getTotalElements());

        when(studentDao.filter(anyString(), anyInt(), anyInt(), any())).thenReturn(expected);
        Page<Student> res2 = studentService.filterStudents("name:Bob", 0, 10, List.of("name,asc"));
        assertEquals(1, res2.getTotalElements());
    }

    @Test
    void testCreateAndUpdateDepartmentNotFound() {
        StudentRequest req = new StudentRequest("David", LocalDate.of(2001, 1, 1), 999L);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.createStudent(req));

        Student existing = new Student(1L, "Old", LocalDate.of(2000, 1, 1), null);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ResourceNotFoundException.class, () -> studentService.updateStudent(1L, req));
    }
}
