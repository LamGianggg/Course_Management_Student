package com.example.studentmanagement;

import com.example.studentmanagement.Dao.*;
import com.example.studentmanagement.HibernateDao.*;
import com.example.studentmanagement.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HibernateDaoInheritanceTest {

    @Autowired
    private IStudentDao studentDao;

    @Autowired
    private IDepartmentDao departmentDao;

    @Autowired
    private IClassInfoDao classInfoDao;

    @Autowired
    private IDocumentDao documentDao;

    @Autowired
    private IAppDetailDao appDetailDao;

    @Autowired
    private StudentHibernateDao studentHibernateDao;

    @Autowired
    private DepartmentHibernateDao departmentHibernateDao;

    @Autowired
    private ClassInfoHibernateDao classInfoHibernateDao;

    @Autowired
    private DocumentHibernateDao documentHibernateDao;

    @Autowired
    private AppDetailHibernateDao appDetailHibernateDao;

    @Test
    void testDirectInheritanceOfHibernateDao() {
        // Assert that concrete DAOs directly inherit from HibernateDao
        assertTrue(studentHibernateDao instanceof HibernateDao);
        assertTrue(departmentHibernateDao instanceof HibernateDao);
        assertTrue(classInfoHibernateDao instanceof HibernateDao);
        assertTrue(documentHibernateDao instanceof HibernateDao);
        assertTrue(appDetailHibernateDao instanceof HibernateDao);

        // Check configured sort and search fields
        assertEquals(StudentHibernateDao.ALLOWED_SEARCH_FIELDS, studentHibernateDao.getAllowedSearchFieldsConfigured());
        assertEquals(StudentHibernateDao.ALLOWED_SORT_FIELDS, studentHibernateDao.getAllowedSortFields());

        assertEquals(DepartmentHibernateDao.ALLOWED_SEARCH_FIELDS, departmentHibernateDao.getAllowedSearchFieldsConfigured());
        assertEquals(DepartmentHibernateDao.ALLOWED_SORT_FIELDS, departmentHibernateDao.getAllowedSortFields());

        assertEquals(ClassInfoHibernateDao.ALLOWED_SEARCH_FIELDS, classInfoHibernateDao.getAllowedSearchFieldsConfigured());
        assertEquals(ClassInfoHibernateDao.ALLOWED_SORT_FIELDS, classInfoHibernateDao.getAllowedSortFields());

        assertEquals(DocumentHibernateDao.ALLOWED_SEARCH_FIELDS, documentHibernateDao.getAllowedSearchFieldsConfigured());
        assertEquals(DocumentHibernateDao.ALLOWED_SORT_FIELDS, documentHibernateDao.getAllowedSortFields());

        assertEquals(AppDetailHibernateDao.ALLOWED_SEARCH_FIELDS, appDetailHibernateDao.getAllowedSearchFieldsConfigured());
        assertEquals(AppDetailHibernateDao.ALLOWED_SORT_FIELDS, appDetailHibernateDao.getAllowedSortFields());
    }

    @Test
    void testDaoIntegratesHibernateDao() {
        assertNotNull(studentDao.getHibernateDao());
        assertNotNull(departmentDao.getHibernateDao());
        assertNotNull(classInfoDao.getHibernateDao());
        assertNotNull(documentDao.getHibernateDao());
        assertNotNull(appDetailDao.getHibernateDao());
    }

    @Test
    void testStudentDaoFilterExecution() {
        Page<Student> page = studentDao.filter(null, 0, 10, Collections.emptyList());
        assertNotNull(page);
    }

    @Test
    void testDepartmentDaoFilterExecution() {
        Page<Department> page = departmentDao.filter(null, 0, 10, Collections.emptyList());
        assertNotNull(page);
    }

    @Test
    void testClassInfoDaoFilterExecution() {
        Page<ClassInfo> page = classInfoDao.filter(null, 0, 10, Collections.emptyList());
        assertNotNull(page);
    }

    @Test
    void testDocumentDaoFilterExecution() {
        Page<Document> page = documentDao.filter(null, 0, 10, Collections.emptyList());
        assertNotNull(page);
    }

    @Test
    void testAppDetailDaoFilterExecution() {
        Page<AppDetail> page = appDetailDao.filter(null, 0, 10, Collections.emptyList());
        assertNotNull(page);
    }
}
