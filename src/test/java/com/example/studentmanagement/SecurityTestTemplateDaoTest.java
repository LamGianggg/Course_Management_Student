package com.example.studentmanagement;

import com.example.studentmanagement.Dao.ISecurityTestTemplateDao;
import com.example.studentmanagement.entity.SecurityTestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SecurityTestTemplateDaoTest {

    @Autowired
    private ISecurityTestTemplateDao dao;

    @Test
    void testGetSecurityTestList_WithUserAppId() {
        SecurityTestTemplate template = new SecurityTestTemplate(null, "SQL Injection Test", "Test SQLi", "app123", "userApp456", "SQLI", "ACTIVE");
        dao.save(template);

        List<SecurityTestTemplate> result = dao.getSecurityTestList(null, 0, 10, "name", "asc", "app123", "userApp456");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(t -> "SQL Injection Test".equals(t.getName())));
    }

    @Test
    void testGetSecurityTestList_WithoutUserAppId() {
        SecurityTestTemplate template = new SecurityTestTemplate(null, "XSS Test", "Test XSS", "app123", null, "XSS", "ACTIVE");
        dao.save(template);

        List<SecurityTestTemplate> result = dao.getSecurityTestList(null, 0, 10, "name", "desc", "app123", null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(t -> "XSS Test".equals(t.getName())));
    }

    @Test
    void testFindByPkAndSave() {
        SecurityTestTemplate template = new SecurityTestTemplate(null, "New Test", "Desc", "app1", "user1", "TYPE", "STATUS");
        SecurityTestTemplate saved = dao.save(template);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        SecurityTestTemplate found = dao.findByPk(saved.getId());
        assertNotNull(found);
        assertEquals("New Test", found.getName());

        assertNull(dao.findByPk(null));
        assertNull(dao.findByPk(999999L));
    }
}
