package com.example.studentmanagement;

import com.example.studentmanagement.dto.UserInfoWrapper;
import com.example.studentmanagement.entity.SecurityTestTemplate;
import com.example.studentmanagement.entity.UserInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityTestAndUserInfoEntityTest {

    @Test
    void testSecurityTestTemplate() {
        SecurityTestTemplate template = new SecurityTestTemplate();
        template.setId(10L);
        template.setName("Test Template");
        template.setDescription("Desc");
        template.setAppId("app123");
        template.setUserAppId("user456");
        template.setTestType("XSS");
        template.setStatus("ACTIVE");
        LocalDateTime now = LocalDateTime.now();
        template.setCreatedTime(now);

        assertEquals(10L, template.getId());
        assertEquals("Test Template", template.getName());
        assertEquals("Desc", template.getDescription());
        assertEquals("app123", template.getAppId());
        assertEquals("user456", template.getUserAppId());
        assertEquals("XSS", template.getTestType());
        assertEquals("ACTIVE", template.getStatus());
        assertEquals(now, template.getCreatedTime());
    }

    @Test
    void testUserInfoAndWrapper() {
        UserInfo user = new UserInfo();
        user.setId(20L);
        user.setUsername("alice");
        user.setEmail("alice@test.com");
        user.setOrganisation("DEPT_A");
        user.setStatus("APPROVED");
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertEquals(20L, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@test.com", user.getEmail());
        assertEquals("DEPT_A", user.getOrganisation());
        assertEquals("APPROVED", user.getStatus());
        assertEquals(now, user.getCreatedAt());

        UserInfoWrapper wrapper = new UserInfoWrapper(30L, "bob", "bob@test.com", "DEPT_B", "REJECTED");
        assertEquals(30L, wrapper.getId());
        assertEquals("bob", wrapper.getUsername());
        assertEquals("bob@test.com", wrapper.getEmail());
        assertEquals("DEPT_B", wrapper.getOrganisation());
        assertEquals("REJECTED", wrapper.getStatus());
        assertTrue(wrapper.toString().contains("bob"));
    }
}
