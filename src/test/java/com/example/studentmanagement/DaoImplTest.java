package com.example.studentmanagement;

import com.example.studentmanagement.Dao.*;
import com.example.studentmanagement.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DaoImplTest {

    @Autowired
    private IDepartmentDao departmentDao;

    @Autowired
    private IStudentDao studentDao;

    @Autowired
    private IDocumentDao documentDao;

    @Autowired
    private IAppDetailDao appDetailDao;

    @Test
    void testDepartmentDaoImpl() {
        Department dept = new Department(null, "Physics", "Physics Department");
        Department saved = departmentDao.save(dept);
        assertNotNull(saved.getId());

        // findByPk
        assertNotNull(departmentDao.findByPk(saved.getId()));
        assertNull(departmentDao.findByPk(null));

        // existsById
        assertTrue(departmentDao.existsById(saved.getId()));
        assertFalse(departmentDao.existsById(99999L));
        assertFalse(departmentDao.existsById(null));

        // update
        saved.setDescription("Updated Physics");
        Department updated = departmentDao.update(saved);
        assertEquals("Updated Physics", updated.getDescription());

        // save existing
        Department savedAgain = departmentDao.save(updated);
        assertEquals(saved.getId(), savedAgain.getId());

        // getAll
        assertFalse(departmentDao.getAll().isEmpty());

        // findAll pageable
        Page<Department> page = departmentDao.findAll(PageRequest.of(0, 5));
        assertNotNull(page);

        // filter spec
        Specification<Department> spec = (root, q, cb) -> cb.equal(root.get("name"), "Physics");
        Page<Department> filtered = departmentDao.filter(spec, PageRequest.of(0, 5));
        assertEquals(1, filtered.getTotalElements());

        departmentDao.filter((Specification<Department>) null, PageRequest.of(0, 5));

        // delete
        departmentDao.delete(saved.getId());
        assertNull(departmentDao.findByPk(saved.getId()));
    }

    @Test
    void testStudentDaoImpl() {
        Department dept = departmentDao.save(new Department(null, "Chemistry", "Chemistry Dept"));
        Student s = new Student(null, "Le Van C", LocalDate.of(2003, 3, 15), dept);
        Student saved = studentDao.save(s);
        assertNotNull(saved.getId());

        // findByPk
        assertNotNull(studentDao.findByPk(saved.getId()));
        assertNull(studentDao.findByPk(null));

        // update
        saved.setName("Le Van C Updated");
        Student updated = studentDao.update(saved);
        assertEquals("Le Van C Updated", updated.getName());

        // save existing
        Student savedAgain = studentDao.save(updated);
        assertEquals(saved.getId(), savedAgain.getId());

        // getAll
        assertFalse(studentDao.getAll().isEmpty());

        // findByDepartmentId
        assertFalse(studentDao.findByDepartmentId(dept.getId()).isEmpty());
        assertTrue(studentDao.findByDepartmentId(null).isEmpty());

        // findByNameContaining
        assertFalse(studentDao.findByNameContaining("Updated").isEmpty());
        assertTrue(studentDao.findByNameContaining(null).isEmpty());
        assertTrue(studentDao.findByNameContaining("   ").isEmpty());

        // findAll pageable
        Page<Student> page = studentDao.findAll(PageRequest.of(0, 5));
        assertNotNull(page);

        // filter spec
        Specification<Student> spec = (root, q, cb) -> cb.equal(root.get("name"), "Le Van C Updated");
        Page<Student> filtered = studentDao.filter(spec, PageRequest.of(0, 5));
        assertEquals(1, filtered.getTotalElements());

        studentDao.filter((Specification<Student>) null, PageRequest.of(0, 5));

        // delete
        studentDao.delete(saved.getId());
        assertNull(studentDao.findByPk(saved.getId()));
    }

    @Test
    void testDocumentDaoImpl() {
        // Prepare AppDetail first
        AppDetail app = new AppDetail("APP_TEST_1", "Test App", "Test Description");
        appDetailDao.save(app);

        Document doc = new Document("DOC_TEST_1", "APP_TEST_1", "spec.pdf", "/docs/spec.pdf", "alice", 1.0, false, new Date(), new Date(), true);
        Document saved = documentDao.save(doc);
        assertNotNull(saved.getId());

        // findByPk
        assertNotNull(documentDao.findByPk(saved.getId()));
        assertNull(documentDao.findByPk(null));

        // findByPath
        assertNotNull(documentDao.findByPath("/docs/spec.pdf"));
        assertNull(documentDao.findByPath("/docs/non-existent.pdf"));
        assertNull(documentDao.findByPath(null));

        // findByAppId & findByAppIdAndDeletedFalse
        assertFalse(documentDao.findByAppId("APP_TEST_1").isEmpty());
        assertTrue(documentDao.findByAppId(null).isEmpty());
        assertFalse(documentDao.findByAppIdAndDeletedFalse("APP_TEST_1").isEmpty());
        assertTrue(documentDao.findByAppIdAndDeletedFalse(null).isEmpty());

        // findAllActive
        assertFalse(documentDao.findAllActive().isEmpty());

        // update & save existing
        saved.setName("spec_v2.pdf");
        Document updated = documentDao.update(saved);
        assertEquals("spec_v2.pdf", updated.getName());
        documentDao.save(updated);

        // findAll pageable & filter
        Page<Document> page = documentDao.findAll(PageRequest.of(0, 5));
        assertNotNull(page);

        Specification<Document> spec = (root, q, cb) -> cb.equal(root.get("name"), "spec_v2.pdf");
        Page<Document> filtered = documentDao.filter(spec, PageRequest.of(0, 5));
        assertEquals(1, filtered.getTotalElements());

        documentDao.filter((Specification<Document>) null, PageRequest.of(0, 5));

        // delete
        documentDao.delete(saved.getId());
        assertNull(documentDao.findByPk(saved.getId()));
    }

    @Test
    void testAppDetailDaoImpl() {
        AppDetail app = new AppDetail("APP_DETAIL_XYZ", "App XYZ", "XYZ Description");
        AppDetail saved = appDetailDao.save(app);
        assertNotNull(saved.getId());

        // findByPk
        assertNotNull(appDetailDao.findByPk("APP_DETAIL_XYZ"));
        assertNull(appDetailDao.findByPk(null));

        // existsById
        assertTrue(appDetailDao.existsById("APP_DETAIL_XYZ"));
        assertFalse(appDetailDao.existsById("NON_EXISTING"));
        assertFalse(appDetailDao.existsById(null));

        // save existing
        app.setDescription("Updated XYZ");
        appDetailDao.save(app);

        // findAll & filter
        Page<AppDetail> page = appDetailDao.findAll(PageRequest.of(0, 5));
        assertNotNull(page);

        Specification<AppDetail> spec = (root, q, cb) -> cb.equal(root.get("name"), "App XYZ");
        Page<AppDetail> filtered = appDetailDao.filter(spec, PageRequest.of(0, 5));
        assertEquals(1, filtered.getTotalElements());

        appDetailDao.filter((Specification<AppDetail>) null, PageRequest.of(0, 5));
    }
}
