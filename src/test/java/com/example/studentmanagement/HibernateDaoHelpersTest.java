package com.example.studentmanagement;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.dto.SearchCriteria;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HibernateDaoHelpersTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        departmentRepository.deleteAll();

        Department dept = departmentRepository.save(new Department(null, "IT", "Info Tech"));
        studentRepository.save(new Student(null, "Alice", LocalDate.of(2001, 1, 10), dept));
        studentRepository.save(new Student(null, "Bob", LocalDate.of(2003, 5, 20), dept));
        studentRepository.save(new Student(null, "Charlie", LocalDate.of(2004, 8, 15), dept));
    }

    // ==================== 1. Pagination Tests ====================

    @Test
    void testPaginationValid() {
        Pageable pageable = HibernateDao.createPageable(0, 10);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageableWithSort = HibernateDao.createPageable(2, 25, sort);
        assertEquals(2, pageableWithSort.getPageNumber());
        assertEquals(25, pageableWithSort.getPageSize());
        assertEquals(sort, pageableWithSort.getSort());
    }

    @Test
    void testPaginationNegativePage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createPageable(-1, 10));
        assertTrue(ex.getMessage().contains("Page index must be >= 0"));
    }

    @Test
    void testPaginationZeroOrNegativeSize() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createPageable(0, 0));
        assertTrue(ex.getMessage().contains("Page size must be between 1 and 100"));

        IllegalArgumentException exNeg = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createPageable(0, -5));
        assertTrue(exNeg.getMessage().contains("Page size must be between 1 and 100"));
    }

    @Test
    void testPaginationExceedsMaxSize() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createPageable(0, 101));
        assertTrue(ex.getMessage().contains("Page size must be between 1 and 100"));
    }

    // ==================== 2. Sorting Tests ====================

    @Test
    void testSortDefault() {
        Sort sort = HibernateDao.createSort(null, Set.of("id", "name"));
        assertNotNull(sort);
        assertEquals("id: ASC", sort.toString());

        Sort emptySort = HibernateDao.createSort(List.of(), Set.of("id", "name"));
        assertEquals("id: ASC", emptySort.toString());
    }

    @Test
    void testSortCommaFormat() {
        Sort sort = HibernateDao.createSort(List.of("name,asc"), Set.of("id", "name", "dob"));
        assertEquals("name: ASC", sort.toString());

        Sort sortDesc = HibernateDao.createSort(List.of("name,desc"), Set.of("id", "name", "dob"));
        assertEquals("name: DESC", sortDesc.toString());

        Sort multiSort = HibernateDao.createSort(List.of("name,asc", "dob,desc"), Set.of("id", "name", "dob"));
        assertTrue(multiSort.toString().contains("name: ASC"));
        assertTrue(multiSort.toString().contains("dob: DESC"));
    }

    @Test
    void testSortSpaceFormat() {
        Sort sort = HibernateDao.createSort(List.of("name", "desc"), Set.of("id", "name", "dob"));
        assertEquals("name: DESC", sort.toString());
    }

    @Test
    void testSortInvalidField() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createSort(List.of("invalidField,asc"), Set.of("id", "name")));
        assertTrue(ex.getMessage().contains("Sort field 'invalidField' is not allowed"));
    }

    @Test
    void testSortInvalidDirection() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createSort(List.of("name,invalidDir"), Set.of("id", "name")));
        assertTrue(ex.getMessage().contains("Invalid sort direction"));
    }

    // ==================== 3. Query / Specification Tests ====================

    @Test
    void testQueryNullOrEmpty() {
        assertNull(HibernateDao.parse(null));
        assertNull(HibernateDao.parse("   "));
    }

    @Test
    void testQueryValidQuery() {
        Specification<Student> spec = HibernateDao.parse("name:Nguyen,dob>2000-01-01");
        assertNotNull(spec);
    }

    @Test
    void testQueryInvalidSyntax() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.parse("noseparator"));
        assertTrue(ex.getMessage().contains("Invalid search syntax"));
    }

    @Test
    void testQueryInvalidField() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.parse("secretField:secret", Set.of("name", "dob")));
        assertTrue(ex.getMessage().contains("Search field 'secretField' is not allowed"));
    }

    @Test
    void testQueryCustomAllowedFields() {
        Specification<Student> spec = HibernateDao.parse("name:John", Set.of("name"));
        assertNotNull(spec);
    }

    @Test
    void testCreateSpecificationDirectly() {
        Specification<Student> spec = HibernateDao.createSpecification(new SearchCriteria("name", ":", "Alice"));
        assertNotNull(spec);
        List<Student> results = studentRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(0).getName());
    }

    // ==================== 4. Combined Execution Tests (Search + Pagination + Sorting) ====================

    @Test
    void testCombinedCreatePageableWithSortParams() {
        Pageable pageable = HibernateDao.createPageable(0, 10, List.of("name,desc"), Set.of("id", "name", "dob"));
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals("name: DESC", pageable.getSort().toString());
    }

    @Test
    void testCombinedExecuteSearchAndPaginationAndSorting() {
        Page<Student> page = HibernateDao.execute(
                studentRepository,
                "dob>2002-01-01",
                Set.of("id", "name", "dob"),
                0,
                10,
                List.of("name,desc"),
                Set.of("id", "name", "dob")
        );

        assertNotNull(page);
        assertEquals(2, page.getTotalElements()); // Bob (2003) and Charlie (2004)
        assertEquals("Charlie", page.getContent().get(0).getName()); // DESC order: Charlie then Bob
        assertEquals("Bob", page.getContent().get(1).getName());
    }

    // ==================== 5. Reusable Instance HibernateDao<T> Tests ====================

    @Test
    void testInstanceHibernateDaoExecution() {
        HibernateDao<Student> studentDaoHelper = new HibernateDao<>(
                studentRepository,
                Set.of("id", "name", "dob"),
                Set.of("id", "name", "dob")
        );

        Page<Student> result = studentDaoHelper.find("name:Alice", 0, 5, List.of("name,asc"));
        assertEquals(1, result.getTotalElements());
        assertEquals("Alice", result.getContent().get(0).getName());
    }
}
