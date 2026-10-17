package com.example.studentmanagement;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Department department;
    private Student student;

    @BeforeEach
    void setUp() {
        department = new Department(1L, "IT", "Information Tech");
        student = new Student(1L, "Nguyen Van A", LocalDate.of(2003, 1, 1), department);
    }

    @Test
    void testGetAllStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student));
        List<Student> list = studentService.getAllStudents();
        assertEquals(1, list.size());
        assertEquals("Nguyen Van A", list.get(0).getName());
    }

    @Test
    void testGetStudentById_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        Student result = studentService.getStudentById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetStudentById_NotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(99L));
    }

    @Test
    void testGetStudentsByDepartmentId_Success() {
        when(departmentRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.findByDepartmentId(1L)).thenReturn(List.of(student));

        List<Student> list = studentService.getStudentsByDepartmentId(1L);
        assertEquals(1, list.size());
    }

    @Test
    void testGetStudentsByDepartmentId_NotFound() {
        when(departmentRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentsByDepartmentId(99L));
    }

    @Test
    void testSearchStudentsByName() {
        when(studentRepository.findByNameContainingIgnoreCase("Nguyen")).thenReturn(List.of(student));
        List<Student> list = studentService.searchStudentsByName("Nguyen");
        assertEquals(1, list.size());
    }

    @Test
    void testCreateStudent_Success() {
        StudentRequest req = new StudentRequest("Le Van B", LocalDate.of(2002, 5, 10), 1L);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(2L);
            return s;
        });

        Student created = studentService.createStudent(req);
        assertNotNull(created);
        assertEquals(2L, created.getId());
        assertEquals("Le Van B", created.getName());
    }

    @Test
    void testCreateStudent_DepartmentNotFound() {
        StudentRequest req = new StudentRequest("Le Van B", LocalDate.of(2002, 5, 10), 99L);
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.createStudent(req));
    }

    @Test
    void testUpdateStudent_Success() {
        StudentRequest req = new StudentRequest("Nguyen Van A Updated", LocalDate.of(2003, 1, 1), 1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student updated = studentService.updateStudent(1L, req);
        assertEquals("Nguyen Van A Updated", updated.getName());
    }

    @Test
    void testDeleteStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        doNothing().when(studentRepository).delete(student);

        assertDoesNotThrow(() -> studentService.deleteStudent(1L));
        verify(studentRepository, times(1)).delete(student);
    }

    @Test
    void testSearchStudentsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Student> spec = Specification.where(null);
        when(studentRepository.findAll(eq(spec), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(student), pageable, 1));

        Page<Student> page = studentService.searchStudents(spec, pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void testAdditionalQueries() {
        when(studentRepository.findByDobBetween(any(), any())).thenReturn(List.of(student));
        when(studentRepository.findStudentsBornAfter(any())).thenReturn(List.of(student));
        when(studentRepository.findAllWithDepartmentOrdered()).thenReturn(List.of(student));
        when(studentRepository.findStudentsByDeptNameNative("IT")).thenReturn(List.of(student));

        assertEquals(1, studentService.getStudentsByDobRange(LocalDate.of(2000, 1, 1), LocalDate.of(2005, 1, 1)).size());
        assertEquals(1, studentService.getStudentsBornAfter(LocalDate.of(2000, 1, 1)).size());
        assertEquals(1, studentService.getAllWithDepartmentOrdered().size());
        assertEquals(1, studentService.getStudentsByDeptNameNative("IT").size());
    }
}
