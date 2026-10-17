package com.example.studentmanagement.rest;

import com.example.studentmanagement.HibernateDao.DepartmentHibernateDao;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.DepartmentRequest;
import com.example.studentmanagement.dto.PagedResponse;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.ok("Departments retrieved successfully", departments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.ok("Department retrieved successfully", department));
    }

    @GetMapping({"/filter", "/search"})
    public ResponseEntity<ApiResponse<PagedResponse<Department>>> filterDepartments(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> sort) {

        Pageable pageable = HibernateDao.createPageable(page, size, sort, DepartmentHibernateDao.ALLOWED_SORT_FIELDS);
        Specification<Department> spec = HibernateDao.parse(search, DepartmentHibernateDao.ALLOWED_SEARCH_FIELDS);

        Page<Department> resultPage = departmentService.searchDepartments(spec, pageable);
        PagedResponse<Department> pagedResponse = new PagedResponse<>(resultPage);

        return ResponseEntity.ok(ApiResponse.ok("Departments filtered successfully", pagedResponse));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        Department created = departmentService.createDepartment(request);
        return new ResponseEntity<>(ApiResponse.ok("Department created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        Department updated = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Department updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.ok("Department deleted successfully with id: " + id, null));
    }
}
