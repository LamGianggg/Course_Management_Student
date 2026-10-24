package com.example.studentmanagement;

import com.example.studentmanagement.dto.UserInfoWrapper;
import com.example.studentmanagement.entity.UserInfo;
import com.example.studentmanagement.exception.GlobalExceptionHandler;
import com.example.studentmanagement.rest.UserInfoController;
import com.example.studentmanagement.service.IUserInfoService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserInfoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IUserInfoService userInfoService;

    @InjectMocks
    private UserInfoController controller;

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
    void testUpdate() throws Exception {
        UserInfoWrapper wrapper = new UserInfoWrapper(1L, "user1", "user1@test.com", "CS", "APPROVED");
        when(userInfoService.update(any(UserInfoWrapper.class))).thenReturn(Map.of("status", "success"));

        mockMvc.perform(put("/api/user-info/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testCreate() throws Exception {
        UserInfo userInfo = new UserInfo(null, "user2", "user2@test.com", "MATH", "PENDING");
        UserInfo saved = new UserInfo(2L, "user2", "user2@test.com", "MATH", "PENDING");

        when(userInfoService.create(any(UserInfo.class))).thenReturn(saved);

        mockMvc.perform(post("/api/user-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2L));
    }

    @Test
    void testGetById() throws Exception {
        UserInfo userInfo = new UserInfo(3L, "user3", "user3@test.com", "PHYSICS", "ACTIVE");
        when(userInfoService.getById(eq(3L))).thenReturn(userInfo);

        mockMvc.perform(get("/api/user-info/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("user3"));
    }
}
