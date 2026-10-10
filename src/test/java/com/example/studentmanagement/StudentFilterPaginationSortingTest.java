package com.example.studentmanagement;

import com.example.studentmanagement.dto.SearchCriteria;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.specification.StudentSpecification;
import com.example.studentmanagement.specification.StudentSpecificationBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for Search (Specification), Pagination, and Sorting.
 * Covers Phase 1, 2, and 3 requirements.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StudentFilterPaginationSortingTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private MockMvc mockMvc;

    private Department dept1;
    private Department dept2;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        departmentRepository.deleteAll();

        dept1 = new Department(null, "Computer Science", "CS Department");
        dept1 = departmentRepository.save(dept1);

        dept2 = new Department(null, "Mathematics", "Math Department");
        dept2 = departmentRepository.save(dept2);

        // Create test students
        studentRepository.save(new Student(null, "Nguyen Van A", LocalDate.of(2003, 1, 15), dept1));
        studentRepository.save(new Student(null, "Tran Thi B", LocalDate.of(2003, 5, 20), dept1));
        studentRepository.save(new Student(null, "Le Van C", LocalDate.of(2002, 11, 10), dept2));
        studentRepository.save(new Student(null, "Pham Thi D", LocalDate.of(2004, 3, 25), dept2));
        studentRepository.save(new Student(null, "Nguyen Thi E", LocalDate.of(2001, 7, 1), dept1));
    }

    // ===================== PHASE 1: Specification Tests =====================

    @Test
    @Order(1)
    void testSearchByNameContains() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("name:Nguyen");
        List<Student> result = studentRepository.findAll(spec);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getName().contains("Nguyen")));
    }

    @Test
    @Order(2)
    void testSearchByExactId() {
        Student firstStudent = studentRepository.findAll().get(0);
        Specification<Student> spec = StudentSpecificationBuilder.parse("id:" + firstStudent.getId());
        List<Student> result = studentRepository.findAll(spec);
        assertEquals(1, result.size());
        assertEquals(firstStudent.getId(), result.get(0).getId());
    }

    @Test
    @Order(3)
    void testSearchByDobGreaterThan() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("dob>2003-01-01");
        List<Student> result = studentRepository.findAll(spec);
        assertTrue(result.size() >= 2);
        assertTrue(result.stream().allMatch(s -> s.getDob().isAfter(LocalDate.of(2003, 1, 1))));
    }

    @Test
    @Order(4)
    void testSearchByDobLessThan() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("dob<2003-01-01");
        List<Student> result = studentRepository.findAll(spec);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().allMatch(s -> s.getDob().isBefore(LocalDate.of(2003, 1, 1))));
    }

    @Test
    @Order(5)
    void testSearchMultipleConditionsAnd() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("name:Van,dob>2002-01-01");
        List<Student> result = studentRepository.findAll(spec);
        assertTrue(result.stream().allMatch(s ->
                s.getName().toLowerCase().contains("van")
                        && s.getDob().isAfter(LocalDate.of(2002, 1, 1))));
    }

    @Test
    @Order(6)
    void testSearchEmptyReturnsAll() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("");
        assertNull(spec);
        // null spec should return all
        Page<Student> page = studentRepository.findAll(Specification.where(null), PageRequest.of(0, 10));
        assertEquals(5, page.getTotalElements());
    }

    @Test
    @Order(7)
    void testSearchNullReturnsAll() {
        Specification<Student> spec = StudentSpecificationBuilder.parse(null);
        assertNull(spec);
    }

    @Test
    @Order(8)
    void testSearchInvalidFieldThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> StudentSpecificationBuilder.parse("invalidField:value"));
        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    @Order(9)
    void testSearchInvalidSyntaxThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> StudentSpecificationBuilder.parse("noseparator"));
        assertTrue(ex.getMessage().contains("Invalid search syntax"));
    }

    @Test
    @Order(10)
    void testSearchInvalidDobValueThrowsException() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("dob:not-a-date");
        assertThrows(Exception.class, () -> studentRepository.findAll(spec));
    }

    @Test
    @Order(11)
    void testSearchNoResults() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("name:ZZZZZZZ");
        List<Student> result = studentRepository.findAll(spec);
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(12)
    void testSearchByDepartmentName() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("department.name:Computer");
        List<Student> result = studentRepository.findAll(spec);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(s ->
                s.getDepartment().getName().contains("Computer")));
    }

    // ===================== PHASE 2: Pagination Tests =====================

    @Test
    @Order(20)
    void testPaginationFirstPage() {
        Page<Student> page = studentRepository.findAll(
                Specification.where(null), PageRequest.of(0, 2));
        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    @Order(21)
    void testPaginationSecondPage() {
        Page<Student> page = studentRepository.findAll(
                Specification.where(null), PageRequest.of(1, 2));
        assertEquals(2, page.getContent().size());
        assertEquals(1, page.getNumber());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    @Order(22)
    void testPaginationLastPage() {
        Page<Student> page = studentRepository.findAll(
                Specification.where(null), PageRequest.of(2, 2));
        assertEquals(1, page.getContent().size());
        assertTrue(page.isLast());
    }

    @Test
    @Order(23)
    void testPaginationBeyondTotalPages() {
        Page<Student> page = studentRepository.findAll(
                Specification.where(null), PageRequest.of(10, 2));
        assertTrue(page.getContent().isEmpty());
        assertEquals(5, page.getTotalElements());
    }

    @Test
    @Order(24)
    void testPaginationDifferentSize() {
        Page<Student> page3 = studentRepository.findAll(
                Specification.where(null), PageRequest.of(0, 3));
        assertEquals(3, page3.getContent().size());
        assertEquals(2, page3.getTotalPages());

        Page<Student> page5 = studentRepository.findAll(
                Specification.where(null), PageRequest.of(0, 5));
        assertEquals(5, page5.getContent().size());
        assertEquals(1, page5.getTotalPages());
    }

    @Test
    @Order(25)
    void testSearchWithPagination() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("name:Nguyen");
        Page<Student> page = studentRepository.findAll(spec, PageRequest.of(0, 1));
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    // ===================== PHASE 3: Sorting Tests =====================

    @Test
    @Order(30)
    void testSortByNameAscending() {
        Page<Student> page = studentRepository.findAll(
                Specification.where(null),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        List<Student> students = page.getContent();
        for (int i = 1; i < students.size(); i++) {
            assertTrue(students.get(i - 1).getName().compareTo(students.get(i).getName()) <= 0);
        }
    }

    @Test
    @Order(31)
    void testSortByNameDescending() {
        Page<Student> page = studentRepository.findAll(
                Specification.where(null),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name")));
        List<Student> students = page.getContent();
        for (int i = 1; i < students.size(); i++) {
            assertTrue(students.get(i - 1).getName().compareTo(students.get(i).getName()) >= 0);
        }
    }

    @Test
    @Order(32)
    void testSortByDobAscending() {
        Page<Student> page = studentRepository.findAll(
                Specification.where(null),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "dob")));
        List<Student> students = page.getContent();
        for (int i = 1; i < students.size(); i++) {
            assertFalse(students.get(i - 1).getDob().isAfter(students.get(i).getDob()));
        }
    }

    @Test
    @Order(33)
    void testMultipleSortOrders() {
        // Sort by department name (via id proxy) then by name
        Page<Student> page = studentRepository.findAll(
                Specification.where(null),
                PageRequest.of(0, 10, Sort.by(
                        Sort.Order.asc("dob"),
                        Sort.Order.asc("name")
                )));
        List<Student> students = page.getContent();
        assertFalse(students.isEmpty());
        // Verify ordered by dob ascending
        for (int i = 1; i < students.size(); i++) {
            assertTrue(students.get(i - 1).getDob().compareTo(students.get(i).getDob()) <= 0);
        }
    }

    @Test
    @Order(34)
    void testSearchWithPaginationAndSorting() {
        Specification<Student> spec = StudentSpecificationBuilder.parse("dob>2002-01-01");
        Page<Student> page = studentRepository.findAll(spec,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "name")));

        assertEquals(2, page.getContent().size());
        assertTrue(page.getTotalElements() >= 2);
        // Verify sorted by name
        assertTrue(page.getContent().get(0).getName()
                .compareTo(page.getContent().get(1).getName()) <= 0);
    }

    // ===================== Controller / Integration Tests =====================

    @Test
    @Order(40)
    void testFilterEndpointNoSearch() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @Order(41)
    void testFilterEndpointWithSearch() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("search", "name:Nguyen")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @Order(42)
    void testFilterEndpointWithSort() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(43)
    void testFilterEndpointWithSearchAndPaginationAndSort() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("search", "dob>2002-01-01")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(2));
    }

    @Test
    @Order(44)
    void testFilterEndpointInvalidSearchField() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("search", "invalidField:value"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("not allowed")));
    }

    @Test
    @Order(45)
    void testFilterEndpointInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("not allowed")));
    }

    @Test
    @Order(46)
    void testFilterEndpointInvalidSearchSyntax() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("search", "noseparator"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Invalid search syntax")));
    }

    @Test
    @Order(47)
    void testFilterEndpointPaginationMetadata() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false));
    }

    @Test
    @Order(48)
    void testFilterEndpointEmptySearchReturnsAll() throws Exception {
        mockMvc.perform(get("/api/students/filter")
                        .param("search", "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(5));
    }

    @Test
    @Order(49)
    void testExistingEndpointsStillWork() throws Exception {
        // Verify existing GET /api/students still works
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
