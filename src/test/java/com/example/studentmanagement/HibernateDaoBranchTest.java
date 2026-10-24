package com.example.studentmanagement;

import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateGenericDao;
import com.example.studentmanagement.dto.SearchCriteria;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.Document;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HibernateDaoBranchTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private com.example.studentmanagement.Dao.IDocumentDao documentDao;

    @Test
    void testParseAndOperatorsBranches() {
        Department dept = departmentRepository.save(new Department(null, "Math", "Math Dept"));
        studentRepository.save(new Student(null, "Alice", LocalDate.of(2001, 1, 1), dept));
        studentRepository.save(new Student(null, "Bob", LocalDate.of(2003, 5, 20), dept));

        // empty commas in search
        Specification<Student> spec1 = HibernateDao.parse("name:Alice,  ,dob>2000-01-01");
        assertNotNull(spec1);
        assertEquals(1, studentRepository.findAll(spec1).size());

        // null allowedFields (no restriction)
        Specification<Student> spec2 = HibernateDao.parse("name:Alice", null);
        assertNotNull(spec2);

        // empty allowedFields (no restriction)
        Specification<Student> spec3 = HibernateDao.parse("name:Alice", Collections.emptySet());
        assertNotNull(spec3);

        // nested join: department.name
        Specification<Student> specJoin = HibernateDao.parse("department.name:Math");
        assertNotNull(specJoin);
        assertEquals(2, studentRepository.findAll(specJoin).size());

        // operator <: less than
        Specification<Student> specLess = HibernateDao.parse("dob<2002-01-01");
        assertEquals(1, studentRepository.findAll(specLess).size());

        // operator !: not equal
        Specification<Student> specNot = HibernateDao.parse("name!Alice");
        assertEquals(1, studentRepository.findAll(specNot).size());

        // operator : on non-string (Long id)
        Long studentId = studentRepository.findAll().get(0).getId();
        Specification<Student> specId = HibernateDao.parse("id:" + studentId);
        assertEquals(1, studentRepository.findAll(specId).size());

        // invalid conversions evaluated when query executed
        Specification<Student> specBadNum = HibernateDao.parse("id:notANumber");
        assertThrows(Exception.class, () -> studentRepository.findAll(specBadNum));

        Specification<Student> specBadDate = HibernateDao.parse("dob:notADate");
        assertThrows(Exception.class, () -> studentRepository.findAll(specBadDate));

        // unsupported operator in criteria
        Specification<Student> badOpSpec = HibernateDao.createSpecification(new SearchCriteria("name", "~", "Alice"));
        assertThrows(Exception.class, () -> studentRepository.findAll(badOpSpec));
    }

    @Test
    void testDateConversionsInDocument() {
        // Document has creationTime (Date) and active (Boolean) and version (Double)
        Specification<Document> specDateSpace = HibernateDao.parse("creationTime>2020-01-01 00:00:00", Set.of("creationTime"));
        assertNotNull(specDateSpace);

        Specification<Document> specDateIso = HibernateDao.parse("creationTime>2020-01-01T00:00:00Z", Set.of("creationTime"));
        assertNotNull(specDateIso);

        Specification<Document> specDateDay = HibernateDao.parse("creationTime>2020-01-01", Set.of("creationTime"));
        assertNotNull(specDateDay);

        Specification<Document> specBool = HibernateDao.parse("active:true", Set.of("active"));
        assertNotNull(specBool);

        Specification<Document> specDouble = HibernateDao.parse("version>0.5", Set.of("version"));
        assertNotNull(specDouble);

        Specification<Document> specBadDocDate = HibernateDao.parse("creationTime:invalid-date", Set.of("creationTime"));
        assertThrows(Exception.class, () -> documentDao.filter(specBadDocDate, PageRequest.of(0, 5)));
    }

    @Test
    void testSortBranches() {
        // with comma: no direction specified -> default ASC
        Sort s1 = HibernateDao.createSort(List.of("name"), Set.of("name"));
        assertEquals("name: ASC", s1.toString());

        // without comma: ["name", "asc"]
        Sort s2 = HibernateDao.createSort(List.of("name", "asc"), Set.of("name"));
        assertEquals("name: ASC", s2.toString());

        // without comma: ["name", "desc"]
        Sort s3 = HibernateDao.createSort(List.of("name", "desc"), Set.of("name"));
        assertEquals("name: DESC", s3.toString());

        // without comma: ["name"] (no next param)
        Sort s4 = HibernateDao.createSort(List.of("name"), (Set<String>) null);
        assertEquals("name: ASC", s4.toString());

        // without comma: ["name", "id"] (next param is another field, not asc/desc)
        Sort s5 = HibernateDao.createSort(List.of("name", "id"), (Set<String>) null);
        assertTrue(s5.toString().contains("name: ASC"));
        assertTrue(s5.toString().contains("id: ASC"));

        // fallback customization
        Sort s6 = HibernateDao.createSort(null, null, "customField", Sort.Direction.DESC);
        assertEquals("customField: DESC", s6.toString());

        Sort s7 = HibernateDao.createSort(List.of(), null, null, null);
        assertEquals("id: ASC", s7.toString());
    }

    @Test
    void testExecuteAndPageableOverloads() {
        Pageable p1 = HibernateDao.createPageable(0, 10, List.of("name,asc"), Set.of("name"));
        assertNotNull(p1);

        Page<Student> page1 = HibernateDao.execute(studentRepository, "name:Alice", 0, 10, List.of("name,asc"));
        assertNotNull(page1);

        Page<Student> page2 = HibernateDao.execute(studentRepository, "name:Alice", Set.of("name"), 0, 10, List.of("name,asc"), Set.of("name"));
        assertNotNull(page2);

        Page<Student> page3 = HibernateDao.execute(studentRepository, (String) null, 0, 10, (List<String>) null);
        assertNotNull(page3);
    }

    @Test
    void testHibernateDaoInstanceConstructorsAndMethods() {
        // default constructor
        HibernateDao<Student> h1 = new HibernateDao<>();
        assertThrows(IllegalStateException.class, () -> h1.find("name:Alice", 0, 10, null));
        assertThrows(IllegalStateException.class, () -> h1.find((Specification<Student>) null, 0, 10, null));

        // 1-arg constructor
        HibernateDao<Student> h2 = new HibernateDao<>(studentRepository);
        assertNotNull(h2.getRepository());

        // 3-arg constructor
        HibernateDao<Student> h3 = new HibernateDao<>(studentRepository, Set.of("name"), Set.of("name"));
        assertNotNull(h3.getAllowedSearchFieldsConfigured());
        assertNotNull(h3.getAllowedSortFields());

        // 5-arg constructor with null defaults
        HibernateDao<Student> h4 = new HibernateDao<>(studentRepository, Set.of("name"), Set.of("name"), null, null);
        assertEquals("id", h4.getDefaultSortField());
        assertEquals(Sort.Direction.ASC, h4.getDefaultSortDirection());

        // setters & getters
        h4.setRepository(studentRepository);
        h4.setAllowedSearchFields(Set.of("id"));
        h4.setAllowedSortFields(Set.of("id"));
        h4.setDefaultSortField("id");
        h4.setDefaultSortDirection(Sort.Direction.DESC);
        assertEquals("id", h4.getDefaultSortField());
        assertEquals(Sort.Direction.DESC, h4.getDefaultSortDirection());
        assertEquals(Set.of("id"), h4.getAllowedSearchFieldsConfigured());
        assertEquals(Set.of("id"), h4.getAllowedSortFields());
        assertNotNull(HibernateDao.getAllowedSearchFields());

        // find with custom spec
        Page<Student> pageCustom = h2.find((Specification<Student>) null, 0, 10, null);
        assertNotNull(pageCustom);

        Page<Student> pageSpec = h2.find((root, q, cb) -> cb.conjunction(), 0, 10, List.of("id,asc"));
        assertNotNull(pageSpec);
    }

    @Test
    void testHibernateGenericDaoBranches() {
        // constructor 1
        HibernateGenericDao<Long, Department> dao1 = new HibernateGenericDao<>(Department.class, entityManager);
        assertEquals(Department.class, dao1.getType());
        assertNotNull(dao1.getEntityManager());
        assertNotNull(dao1.getHibernateDao());

        // constructor 2 with null hibernateDao
        HibernateGenericDao<Long, Department> dao2 = new HibernateGenericDao<>(Department.class, entityManager, null);
        assertNotNull(dao2.getHibernateDao());

        // constructor 2 with hibernateDao having null repository
        HibernateDao<Department> customDao = new HibernateDao<>();
        HibernateGenericDao<Long, Department> dao3 = new HibernateGenericDao<>(Department.class, entityManager, customDao);
        assertNotNull(customDao.getRepository());

        // filter with spec == null vs != null
        Page<Department> p1 = dao1.filter((Specification<Department>) null, PageRequest.of(0, 5));
        assertNotNull(p1);
        Page<Department> p2 = dao1.filter((root, q, cb) -> cb.conjunction(), PageRequest.of(0, 5));
        assertNotNull(p2);

        // filter search with hibernateDao == null
        dao1.setHibernateDao(null);
        Page<Department> p3 = dao1.filter(null, 0, 5, null);
        assertNotNull(p3);

        // setHibernateDao with non-null
        dao1.setHibernateDao(new HibernateDao<>());
        assertNotNull(dao1.getHibernateDao().getRepository());
    }

    @Test
    void testPageableAndSortValidationBranches() {
        // createPageable bound checks
        assertThrows(IllegalArgumentException.class, () -> HibernateDao.createPageable(0, 0));
        assertThrows(IllegalArgumentException.class, () -> HibernateDao.createPageable(0, 101));
        assertThrows(IllegalArgumentException.class, () -> HibernateDao.createPageable(-1, 10));
        assertNotNull(HibernateDao.createPageable(0, 20));

        // invalid direction in comma format
        assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createSort(List.of("name,not_a_direction"), Set.of("name")));

        // disallowed sort field in comma format
        assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createSort(List.of("forbidden,asc"), Set.of("name")));

        // disallowed sort field in space/list format
        assertThrows(IllegalArgumentException.class,
                () -> HibernateDao.createSort(List.of("forbidden", "asc"), Set.of("name")));

        // default createSort overload with 1 arg
        Sort defaultSort = HibernateDao.createSort(List.of("name,desc"));
        assertEquals("name: DESC", defaultSort.toString());
    }

    @Test
    void testConvertValueAndExpectedFormatViaReflection() throws Exception {
        Method convertValueMethod = HibernateDao.class.getDeclaredMethod("convertValue", String.class, Class.class);
        convertValueMethod.setAccessible(true);

        Method getExpectedFormatMethod = HibernateDao.class.getDeclaredMethod("getExpectedFormat", Class.class);
        getExpectedFormatMethod.setAccessible(true);

        Method parseDateMethod = HibernateDao.class.getDeclaredMethod("parseDate", String.class);
        parseDateMethod.setAccessible(true);

        // test getExpectedFormat branches
        assertEquals("yyyy-MM-dd", getExpectedFormatMethod.invoke(null, LocalDate.class));
        assertEquals("yyyy-MM-dd or yyyy-MM-dd HH:mm:ss", getExpectedFormatMethod.invoke(null, Date.class));
        assertEquals("numeric (e.g. 123)", getExpectedFormatMethod.invoke(null, Long.class));
        assertEquals("numeric (e.g. 123)", getExpectedFormatMethod.invoke(null, long.class));
        assertEquals("integer (e.g. 42)", getExpectedFormatMethod.invoke(null, Integer.class));
        assertEquals("integer (e.g. 42)", getExpectedFormatMethod.invoke(null, int.class));
        assertEquals("decimal (e.g. 1.0)", getExpectedFormatMethod.invoke(null, Double.class));
        assertEquals("decimal (e.g. 1.0)", getExpectedFormatMethod.invoke(null, double.class));
        assertEquals("true/false", getExpectedFormatMethod.invoke(null, Boolean.class));
        assertEquals("true/false", getExpectedFormatMethod.invoke(null, boolean.class));
        assertEquals("String", getExpectedFormatMethod.invoke(null, String.class));

        // test parseDate branches
        assertNotNull(parseDateMethod.invoke(null, "2020-01-01 10:00:00"));
        assertNotNull(parseDateMethod.invoke(null, "2020-01-01T10:00:00Z"));
        assertNotNull(parseDateMethod.invoke(null, "2020-01-01"));
        assertThrows(InvocationTargetException.class, () -> parseDateMethod.invoke(null, "invalid-date"));

        // test convertValue branches
        assertEquals(123L, convertValueMethod.invoke(null, "123", Long.class));
        assertEquals(123L, convertValueMethod.invoke(null, "123", long.class));
        assertEquals(42, convertValueMethod.invoke(null, "42", Integer.class));
        assertEquals(42, convertValueMethod.invoke(null, "42", int.class));
        assertEquals(true, convertValueMethod.invoke(null, "true", Boolean.class));
        assertEquals(false, convertValueMethod.invoke(null, "false", boolean.class));
        assertEquals(LocalDate.of(2021, 5, 1), convertValueMethod.invoke(null, "2021-05-01", LocalDate.class));
        assertEquals(3.14, convertValueMethod.invoke(null, "3.14", Double.class));
        assertEquals(3.14, convertValueMethod.invoke(null, "3.14", double.class));
        assertNotNull(convertValueMethod.invoke(null, "2021-05-01 10:00:00", java.sql.Timestamp.class));
        assertNotNull(convertValueMethod.invoke(null, "2021-05-01", java.sql.Date.class));
        assertNotNull(convertValueMethod.invoke(null, "2021-05-01", Date.class));
        assertEquals("rawString", convertValueMethod.invoke(null, "rawString", String.class));

        // invalid conversions triggering getExpectedFormat
        assertThrows(InvocationTargetException.class, () -> convertValueMethod.invoke(null, "abc", Long.class));
        assertThrows(InvocationTargetException.class, () -> convertValueMethod.invoke(null, "abc", long.class));
        assertThrows(InvocationTargetException.class, () -> convertValueMethod.invoke(null, "abc", Integer.class));
        assertThrows(InvocationTargetException.class, () -> convertValueMethod.invoke(null, "abc", int.class));
        assertThrows(InvocationTargetException.class, () -> convertValueMethod.invoke(null, "abc", Double.class));
        assertThrows(InvocationTargetException.class, () -> convertValueMethod.invoke(null, "abc", double.class));
        assertThrows(InvocationTargetException.class, () -> convertValueMethod.invoke(null, "abc", LocalDate.class));
    }
}
