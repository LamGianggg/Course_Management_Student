package com.example.studentmanagement;

import com.example.studentmanagement.dto.ClassInfoWrapper;
import com.example.studentmanagement.entity.ClassInfo;
import com.example.studentmanagement.rest.Impl.ClassInfoRestImpl;
import com.example.studentmanagement.service.ClassInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassInfoRestTest {

    @Mock
    private ClassInfoService classInfoService;

    @InjectMocks
    private ClassInfoRestImpl classInfoRest;

    private ClassInfo classInfo;
    private ClassInfoWrapper wrapper;

    @BeforeEach
    void setUp() {
        classInfo = new ClassInfo("c-1", "Java Course", "IT", "Desc", LocalDateTime.now());
        wrapper = new ClassInfoWrapper("Java Course", "IT", "Desc");
    }

    @Test
    void testCreateClassInfo() {
        when(classInfoService.create(any())).thenReturn(classInfo);
        ResponseEntity<ClassInfo> response = classInfoRest.createClassInfo(wrapper);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateClassInfo() {
        when(classInfoService.update(any())).thenReturn(classInfo);
        ResponseEntity<ClassInfo> response = classInfoRest.updateClassInfo(wrapper);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testDeleteClassInfo() {
        doNothing().when(classInfoService).delete("c-1");
        ResponseEntity<String> response = classInfoRest.deleteClassInfo("c-1");
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetClassInfoById() {
        when(classInfoService.getById("c-1")).thenReturn(classInfo);
        ResponseEntity<ClassInfo> response = classInfoRest.getClassInfoById("c-1");
        assertEquals(200, response.getStatusCode().value());

        when(classInfoService.getById("c-unknown")).thenReturn(null);
        assertEquals(404, classInfoRest.getClassInfoById("c-unknown").getStatusCode().value());
    }

    @Test
    void testGetClassInfoByName() {
        when(classInfoService.getByName("Java Course")).thenReturn(classInfo);
        ResponseEntity<ClassInfo> response = classInfoRest.getClassInfoByName("Java Course");
        assertEquals(200, response.getStatusCode().value());

        when(classInfoService.getByName("Unknown")).thenReturn(null);
        assertEquals(404, classInfoRest.getClassInfoByName("Unknown").getStatusCode().value());
    }

    @Test
    void testGetAllClassInfo() {
        when(classInfoService.getAll()).thenReturn(List.of(classInfo));
        ResponseEntity<List<ClassInfo>> response = classInfoRest.getAllClassInfo();
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetClassInfoByDomain() {
        when(classInfoService.getByDomain("IT")).thenReturn(List.of(classInfo));
        ResponseEntity<List<ClassInfo>> response = classInfoRest.getClassInfoByDomain("IT");
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testSearchClassInfo() {
        when(classInfoService.search("Java", 0, 10, "name", "asc")).thenReturn(List.of(classInfo));
        ResponseEntity<List<ClassInfo>> response = classInfoRest.searchClassInfo("Java", 0, 10, "name", "asc");
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetClassInfoCount() {
        when(classInfoService.getCount("Java")).thenReturn(10);
        ResponseEntity<Integer> response = classInfoRest.getClassInfoCount("Java");
        assertEquals(10, response.getBody());
    }

    @Test
    void testFilterClassInfo() {
        when(classInfoService.filterClassInfo(any(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(classInfo)));
        var response = classInfoRest.filterClassInfo("name:Java", 0, 10, List.of("name,asc"));
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void testExceptions() {
        when(classInfoService.create(any())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.createClassInfo(wrapper));

        when(classInfoService.update(any())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.updateClassInfo(wrapper));

        doThrow(new RuntimeException("Error")).when(classInfoService).delete("c-1");
        assertThrows(RuntimeException.class, () -> classInfoRest.deleteClassInfo("c-1"));

        when(classInfoService.getById("c-1")).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.getClassInfoById("c-1"));

        when(classInfoService.getByName("Java Course")).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.getClassInfoByName("Java Course"));

        when(classInfoService.getAll()).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.getAllClassInfo());

        when(classInfoService.getByDomain("IT")).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.getClassInfoByDomain("IT"));

        when(classInfoService.search(any(), any(), any(), any(), any())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.searchClassInfo("Java", 0, 10, "name", "asc"));

        when(classInfoService.getCount(any())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.getClassInfoCount("Java"));

        when(classInfoService.filterClassInfo(any(), anyInt(), anyInt(), any())).thenThrow(new RuntimeException("Error"));
        assertThrows(RuntimeException.class, () -> classInfoRest.filterClassInfo("Java", 0, 10, null));
    }
}
