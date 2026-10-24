package com.example.studentmanagement;

import com.example.studentmanagement.dto.*;
import com.example.studentmanagement.entity.*;
import com.example.studentmanagement.exception.BusinessException;
import com.example.studentmanagement.exception.GlobalExceptionHandler;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DtoAndEntityTest {

    @Test
    void testApiResponse() {
        ApiResponse<String> res1 = ApiResponse.ok("success", "data");
        assertTrue(res1.isSuccess());
        assertEquals("success", res1.getMessage());
        assertEquals("data", res1.getData());
        assertNotNull(res1.getTimestamp());

        ApiResponse<String> res2 = ApiResponse.error("fail");
        assertFalse(res2.isSuccess());
        assertEquals("fail", res2.getMessage());

        LocalDateTime now = LocalDateTime.now();
        ApiResponse<String> res3 = new ApiResponse<>(true, "msg", "data", now);
        res3.setSuccess(false);
        res3.setMessage("newMsg");
        res3.setData("newData");
        res3.setTimestamp(now);
        assertFalse(res3.isSuccess());
        assertEquals("newMsg", res3.getMessage());
        assertEquals("newData", res3.getData());
        assertEquals(now, res3.getTimestamp());

        ApiResponse<String> res4 = ApiResponse.ok("just data");
        assertTrue(res4.isSuccess());
    }

    @Test
    void testSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria("name", ":", "test");
        assertEquals("name", criteria.getKey());
        assertEquals(":", criteria.getOperation());
        assertEquals("test", criteria.getValue());

        criteria.setKey("dob");
        criteria.setOperation(">");
        criteria.setValue("2000-01-01");
        assertEquals("dob", criteria.getKey());
        assertEquals(">", criteria.getOperation());
        assertEquals("2000-01-01", criteria.getValue());
    }

    @Test
    void testPagedResponse() {
        PageImpl<String> springPage = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 10), 2);
        PagedResponse<String> paged = new PagedResponse<>(springPage);
        assertEquals(2, paged.getContent().size());
        assertEquals(0, paged.getPage());
        assertEquals(10, paged.getSize());
        assertEquals(2, paged.getTotalElements());
        assertEquals(1, paged.getTotalPages());
        assertTrue(paged.isFirst());
        assertTrue(paged.isLast());

        PagedResponse<String> pagedEmpty = new PagedResponse<>();
        pagedEmpty.setContent(List.of("c"));
        pagedEmpty.setPage(1);
        pagedEmpty.setSize(5);
        pagedEmpty.setTotalElements(10);
        pagedEmpty.setTotalPages(2);
        pagedEmpty.setFirst(false);
        pagedEmpty.setLast(false);
        assertEquals(1, pagedEmpty.getContent().size());
        assertFalse(pagedEmpty.isFirst());
    }

    @Test
    void testStudentAndDepartment() {
        Department dept = new Department();
        dept.setId(10L);
        dept.setName("Physics");
        dept.setDescription("Physics Dept");
        assertEquals(10L, dept.getId());
        assertEquals("Physics", dept.getName());
        assertEquals("Physics Dept", dept.getDescription());

        Student student = new Student();
        student.setId(100L);
        student.setName("Alice");
        student.setDob(LocalDate.of(2004, 2, 2));
        student.setDepartment(dept);
        assertEquals(100L, student.getId());
        assertEquals("Alice", student.getName());
        assertEquals(LocalDate.of(2004, 2, 2), student.getDob());
        assertEquals(dept, student.getDepartment());
    }

    @Test
    void testClassInfo() {
        ClassInfo ci = new ClassInfo();
        ci.setId("c-1");
        ci.setName("C++");
        ci.setDomain("Dev");
        ci.setDescription("Desc");
        LocalDateTime now = LocalDateTime.now();
        ci.setCreatedTime(now);

        assertEquals("c-1", ci.getId());
        assertEquals("C++", ci.getName());
        assertEquals("Dev", ci.getDomain());
        assertEquals("Desc", ci.getDescription());
        assertEquals(now, ci.getCreatedTime());
    }

    @Test
    void testDocumentAndAppDetail() {
        AppDetail ad = new AppDetail();
        ad.setId("app-1");
        ad.setName("App One");
        ad.setDescription("Desc");
        LocalDateTime now = LocalDateTime.now();
        ad.setCreatedTime(now);

        assertEquals("app-1", ad.getId());
        assertEquals("App One", ad.getName());
        assertEquals("Desc", ad.getDescription());
        assertEquals(now, ad.getCreatedTime());

        Document doc = new Document();
        doc.setId("doc-1");
        doc.setAppId("app-1");
        doc.setName("test.pdf");
        doc.setPath("/path/to/test.pdf");
        doc.setOwner("user");
        doc.setVersion(1.0);
        doc.setActive(true);
        doc.setDeleted(false);
        doc.setCreationTime(new Date());
        doc.setModificationTime(new Date());

        assertEquals("doc-1", doc.getId());
        assertEquals("test.pdf", doc.getName());
        assertEquals(1.0, doc.getVersion());
    }

    @Test
    void testExceptionsAndGlobalHandler() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Object>> res1 = handler.handleResourceNotFound(new ResourceNotFoundException("Not found"));
        assertEquals(HttpStatus.NOT_FOUND, res1.getStatusCode());

        ResponseEntity<ApiResponse<Object>> res2 = handler.handleValidationException(new ValidationException("Validation err"));
        assertEquals(HttpStatus.BAD_REQUEST, res2.getStatusCode());

        ResponseEntity<ApiResponse<Object>> res3 = handler.handleIllegalArgument(new IllegalArgumentException("Illegal arg"));
        assertEquals(HttpStatus.BAD_REQUEST, res3.getStatusCode());

        ResponseEntity<ApiResponse<Object>> res4 = handler.handleBusinessException(new BusinessException("Business err"));
        assertEquals(HttpStatus.BAD_REQUEST, res4.getStatusCode());

        ResponseEntity<ApiResponse<Object>> res5 = handler.handleGeneralException(new RuntimeException("General err"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res5.getStatusCode());
    }

    @Test
    void testUserAndUserDto() {
        User user = new User("admin", "admin@example.com", "pass", "Admin User");
        user.setId(1L);
        user.setEnabled(true);
        user.setRoles(Set.of(RoleType.ROLE_ADMIN));
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertEquals(1L, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals("Admin User", user.getFullName());
        assertTrue(user.isEnabled());
        assertEquals(now, user.getCreatedAt());
        assertEquals(1, user.getRoles().size());

        UserDto dto = new UserDto(user);
        assertEquals(1L, dto.getId());
        assertEquals("admin", dto.getUsername());
        assertEquals("admin@example.com", dto.getEmail());
        assertEquals("Admin User", dto.getFullName());
        assertTrue(dto.isEnabled());
        assertEquals(now, dto.getCreatedAt());
        assertTrue(dto.getRoles().contains("ROLE_ADMIN"));

        // UserDto with null roles
        User userNoRoles = new User();
        userNoRoles.setRoles(null);
        UserDto dtoNoRoles = new UserDto(userNoRoles);
        assertNotNull(dtoNoRoles.getRoles());
        assertTrue(dtoNoRoles.getRoles().isEmpty());

        UserDto emptyDto = new UserDto();
        emptyDto.setId(2L);
        emptyDto.setUsername("user2");
        emptyDto.setEmail("user2@example.com");
        emptyDto.setFullName("User Two");
        emptyDto.setEnabled(false);
        emptyDto.setRoles(Set.of("ROLE_STUDENT"));
        emptyDto.setCreatedAt(now);
        assertEquals(2L, emptyDto.getId());
        assertEquals("user2", emptyDto.getUsername());
        assertEquals("user2@example.com", emptyDto.getEmail());
        assertEquals("User Two", emptyDto.getFullName());
        assertFalse(emptyDto.isEnabled());
        assertEquals(now, emptyDto.getCreatedAt());
        assertEquals(1, emptyDto.getRoles().size());
    }

    @Test
    void testJwtResponse() {
        JwtResponse jwt = new JwtResponse("token123", 1L, "user", "user@test.com", "Full Name", List.of("ROLE_ADMIN"));
        assertEquals("token123", jwt.getToken());
        assertEquals("Bearer", jwt.getType());
        assertEquals(1L, jwt.getId());
        assertEquals("user", jwt.getUsername());
        assertEquals("user@test.com", jwt.getEmail());
        assertEquals("Full Name", jwt.getFullName());
        assertEquals(1, jwt.getRoles().size());

        JwtResponse empty = new JwtResponse();
        empty.setToken("tok");
        empty.setType("Custom");
        empty.setId(2L);
        empty.setUsername("u");
        empty.setEmail("e@e.com");
        empty.setFullName("fn");
        empty.setRoles(List.of());
        assertEquals("tok", empty.getToken());
        assertEquals("Custom", empty.getType());
        assertEquals(2L, empty.getId());
        assertEquals("u", empty.getUsername());
        assertEquals("e@e.com", empty.getEmail());
        assertEquals("fn", empty.getFullName());
        assertTrue(empty.getRoles().isEmpty());
    }

    @Test
    void testClassInfoWrapper() {
        ClassInfoWrapper w = new ClassInfoWrapper("Course", "Domain", "Desc");
        w.setId("id-1");
        assertEquals("id-1", w.getId());
        assertEquals("Course", w.getName());
        assertEquals("Domain", w.getDomain());
        assertEquals("Desc", w.getDescription());

        ClassInfoWrapper empty = new ClassInfoWrapper();
        empty.setId("id-2");
        empty.setName("N");
        empty.setDomain("D");
        empty.setDescription("De");
        assertEquals("id-2", empty.getId());
        assertEquals("N", empty.getName());
        assertEquals("D", empty.getDomain());
        assertEquals("De", empty.getDescription());
    }

    @Test
    void testRoleType() {
        assertEquals("ADMIN", RoleType.ROLE_ADMIN.getCleanName());
        assertEquals("LECTURER", RoleType.ROLE_LECTURER.getCleanName());
        assertEquals("STUDENT", RoleType.ROLE_STUDENT.getCleanName());
    }
}
