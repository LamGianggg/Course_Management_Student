package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IUserInfoDao;
import com.example.studentmanagement.dto.UserInfoWrapper;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.entity.UserInfo;
import com.example.studentmanagement.exception.ValidationException;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.UserRepository;
import com.example.studentmanagement.service.impl.UserInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserInfoServiceTest {

    @Mock
    private IUserInfoDao userInfoDao;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    private UserInfo existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new UserInfo(1L, "john_doe", "john@example.com", "IT_DEPT", "PENDING");
    }

    @Test
    void testUpdate_NullId_ThrowsException() {
        UserInfoWrapper wrapper = new UserInfoWrapper(null, "user", "email@test.com", "ORG", "APPROVED");
        ValidationException ex = assertThrows(ValidationException.class, () -> userInfoService.update(wrapper));
        assertEquals(UserInfoServiceImpl.ID_FIELD_CANNOT_BE_EMPTY, ex.getMessage());
    }

    @Test
    void testUpdate_UserNotFound_ThrowsException() {
        UserInfoWrapper wrapper = new UserInfoWrapper(99L, "user", "email@test.com", "ORG", "APPROVED");
        when(userInfoDao.findByPk(99L)).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class, () -> userInfoService.update(wrapper));
        assertEquals(UserInfoServiceImpl.NO_USER_WITH_SUCH_ID, ex.getMessage());
    }

    @Test
    void testUpdate_StatusRejected_Success() {
        UserInfoWrapper wrapper = new UserInfoWrapper(1L, "john_doe", "john@example.com", "IT_DEPT", "REJECTED");
        when(userInfoDao.findByPk(1L)).thenReturn(existingUser);
        when(userInfoDao.update(any(UserInfo.class))).thenReturn(existingUser);

        Map<String, String> res = userInfoService.update(wrapper);
        assertNotNull(res);
        assertEquals(UserInfoServiceImpl.SUCCESS, res.get(UserInfoServiceImpl.STATUS));
        assertEquals("REJECTED", existingUser.getStatus());
        verify(userInfoDao).update(existingUser);
    }

    @Test
    void testUpdate_Approved_NewOrganization_Success() {
        UserInfoWrapper wrapper = new UserInfoWrapper(1L, "john_doe", "john@example.com", "NEW_DEPT", "APPROVED");
        when(userInfoDao.findByPk(1L)).thenReturn(existingUser);
        when(departmentRepository.findAll()).thenReturn(List.of(new Department(1L, "OTHER_DEPT", "Desc")));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");

        Map<String, String> res = userInfoService.update(wrapper);
        assertNotNull(res);
        assertEquals(UserInfoServiceImpl.SUCCESS, res.get(UserInfoServiceImpl.STATUS));
        verify(departmentRepository).save(any(Department.class));
        verify(userRepository).save(any());
        verify(userInfoDao).update(existingUser);
    }

    @Test
    void testUpdate_Approved_ExistingOrganization_Success() {
        UserInfoWrapper wrapper = new UserInfoWrapper(1L, "john_doe", "john@example.com", "IT_DEPT", null);
        when(userInfoDao.findByPk(1L)).thenReturn(existingUser);
        when(departmentRepository.findAll()).thenReturn(List.of(new Department(1L, "IT_DEPT", "Desc")));
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        Map<String, String> res = userInfoService.update(wrapper);
        assertNotNull(res);
        assertEquals(UserInfoServiceImpl.SUCCESS, res.get(UserInfoServiceImpl.STATUS));
        verify(departmentRepository, never()).save(any(Department.class));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateAndGetById() {
        when(userInfoDao.save(existingUser)).thenReturn(existingUser);
        when(userInfoDao.findByPk(1L)).thenReturn(existingUser);

        UserInfo created = userInfoService.create(existingUser);
        UserInfo found = userInfoService.getById(1L);

        assertEquals(existingUser, created);
        assertEquals(existingUser, found);
    }
}
