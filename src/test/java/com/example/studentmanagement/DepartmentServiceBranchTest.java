package com.example.studentmanagement;

import com.example.studentmanagement.Dao.IDepartmentDao;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceBranchTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private IDepartmentDao departmentDao;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void testSearchAndFilterWithDepartmentDaoNull() {
        DepartmentServiceImpl service = new DepartmentServiceImpl(departmentRepository);
        // departmentDao is null
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Department> spec = (root, q, cb) -> cb.conjunction();

        Page<Department> searchResult = service.searchDepartments(spec, pageable);
        assertNotNull(searchResult);
        assertTrue(searchResult.isEmpty());

        Page<Department> filterResult = service.filterDepartments("name:IT", 0, 10, List.of("name,asc"));
        assertNotNull(filterResult);
        assertTrue(filterResult.isEmpty());
    }

    @Test
    void testSearchAndFilterWithDepartmentDaoNotNull() {
        Department dept = new Department(1L, "IT", "Information Tech");
        Page<Department> expectedPage = new PageImpl<>(List.of(dept));

        departmentService.setDepartmentDao(departmentDao);

        when(departmentDao.filter(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);
        Page<Department> searchResult = departmentService.searchDepartments((root, q, cb) -> cb.conjunction(), PageRequest.of(0, 10));
        assertEquals(1, searchResult.getTotalElements());

        when(departmentDao.filter(anyString(), anyInt(), anyInt(), any())).thenReturn(expectedPage);
        Page<Department> filterResult = departmentService.filterDepartments("name:IT", 0, 10, List.of("name,asc"));
        assertEquals(1, filterResult.getTotalElements());
    }
}
