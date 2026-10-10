package com.example.studentmanagement.rest;

import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.DepartmentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
