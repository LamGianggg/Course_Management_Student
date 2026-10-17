package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IClassInfoDao;
import com.example.studentmanagement.dto.ClassInfoWrapper;
import com.example.studentmanagement.entity.ClassInfo;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.service.impl.ClassInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassInfoServiceTest {

    @Mock
    private IClassInfoDao classInfoDao;

    @InjectMocks
    private ClassInfoServiceImpl classInfoService;

    private ClassInfo classInfo;

    @BeforeEach
    void setUp() {
        classInfo = new ClassInfo("uuid-1", "Java Class", "Software", "Java desc", LocalDateTime.now());
    }

    @Test
    void testCreate() {
        ClassInfoWrapper wrapper = new ClassInfoWrapper("Java Class", "Software", "Java desc");
        when(classInfoDao.save(any(ClassInfo.class))).thenReturn(classInfo);

        ClassInfo created = classInfoService.create(wrapper);
        assertNotNull(created);
        assertEquals("Java Class", created.getName());
    }

    @Test
    void testUpdate_Success() {
        ClassInfoWrapper wrapper = new ClassInfoWrapper("Java Class Updated", "Software", "Java desc");
        wrapper.setId("uuid-1");
        when(classInfoDao.getById("uuid-1")).thenReturn(classInfo);
        when(classInfoDao.update(any(ClassInfo.class))).thenReturn(classInfo);

        ClassInfo updated = classInfoService.update(wrapper);
        assertNotNull(updated);
    }

    @Test
    void testUpdate_IdNull() {
        ClassInfoWrapper wrapper = new ClassInfoWrapper("Java Class Updated", "Software", "Java desc");
        assertThrows(IllegalArgumentException.class, () -> classInfoService.update(wrapper));
    }

    @Test
    void testUpdate_NotFound() {
        ClassInfoWrapper wrapper = new ClassInfoWrapper("Java Class Updated", "Software", "Java desc");
        wrapper.setId("uuid-unknown");
        when(classInfoDao.getById("uuid-unknown")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> classInfoService.update(wrapper));
    }

    @Test
    void testDelete_Success() {
        when(classInfoDao.getById("uuid-1")).thenReturn(classInfo);
        doNothing().when(classInfoDao).delete("uuid-1");

        assertDoesNotThrow(() -> classInfoService.delete("uuid-1"));
        verify(classInfoDao, times(1)).delete("uuid-1");
    }

    @Test
    void testDelete_NotFound() {
        when(classInfoDao.getById("uuid-unknown")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> classInfoService.delete("uuid-unknown"));
    }

    @Test
    void testGetById_Success() {
        when(classInfoDao.getById("uuid-1")).thenReturn(classInfo);
        ClassInfo found = classInfoService.getById("uuid-1");
        assertEquals("Java Class", found.getName());
    }

    @Test
    void testGetByName() {
        when(classInfoDao.getByName("Java Class")).thenReturn(classInfo);
        ClassInfo found = classInfoService.getByName("Java Class");
        assertEquals("uuid-1", found.getId());
    }

    @Test
    void testGetAll() {
        when(classInfoDao.getAll()).thenReturn(List.of(classInfo));
        List<ClassInfo> list = classInfoService.getAll();
        assertEquals(1, list.size());
    }

    @Test
    void testGetByDomain() {
        when(classInfoDao.getByDomain("Software")).thenReturn(List.of(classInfo));
        List<ClassInfo> list = classInfoService.getByDomain("Software");
        assertEquals(1, list.size());
    }

    @Test
    void testSearch() {
        when(classInfoDao.search("Java", 0, 10, "name", "asc")).thenReturn(List.of(classInfo));
        List<ClassInfo> list = classInfoService.search("Java", 0, 10, "name", "asc");
        assertEquals(1, list.size());
    }

    @Test
    void testGetCount() {
        when(classInfoDao.getCount("Java")).thenReturn(1);
        assertEquals(1, classInfoService.getCount("Java"));
    }
}
