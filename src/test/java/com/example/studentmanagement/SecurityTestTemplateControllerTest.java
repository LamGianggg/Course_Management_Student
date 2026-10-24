package com.example.studentmanagement;

import com.example.studentmanagement.Dao.ISecurityTestTemplateDao;
import com.example.studentmanagement.entity.SecurityTestTemplate;
import com.example.studentmanagement.exception.GlobalExceptionHandler;
import com.example.studentmanagement.rest.SecurityTestTemplateController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SecurityTestTemplateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ISecurityTestTemplateDao securityTestTemplateDao;

    @InjectMocks
    private SecurityTestTemplateController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void testGetSecurityTestList() throws Exception {
        SecurityTestTemplate template = new SecurityTestTemplate(1L, "SQLi Test", "Desc", "app1", "user1", "SQLI", "ACTIVE");
        when(securityTestTemplateDao.getSecurityTestList(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(template));

        mockMvc.perform(get("/api/security-tests")
                        .param("appId", "app1")
                        .param("userAppId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("SQLi Test"));
    }

    @Test
    void testCreate() throws Exception {
        SecurityTestTemplate template = new SecurityTestTemplate(null, "XSS Test", "Desc", "app1", null, "XSS", "ACTIVE");
        SecurityTestTemplate saved = new SecurityTestTemplate(2L, "XSS Test", "Desc", "app1", null, "XSS", "ACTIVE");

        when(securityTestTemplateDao.save(any(SecurityTestTemplate.class))).thenReturn(saved);

        mockMvc.perform(post("/api/security-tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2L));
    }
}
