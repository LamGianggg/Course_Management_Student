package com.example.studentmanagement.rest;

import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.PagedResponse;
import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.service.StudentService;
import com.example.studentmanagement.HibernateDao.HibernateDao;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    /**
     * Whitelist of fields that clients are allowed to sort by.
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "dob"
    );

    /**
     * Whitelist of fields that clients are allowed to search by.
     */
    private static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "id", "name", "dob", "department.name"
    );

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.ok("Students retrieved successfully", students));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.ok("Student retrieved successfully", student));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByDepartment(@PathVariable Long departmentId) {
        List<Student> students = studentService.getStudentsByDepartmentId(departmentId);
        return ResponseEntity.ok(ApiResponse.ok("Students by department retrieved successfully", students));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Student>>> searchStudents(@RequestParam String keyword) {
        List<Student> students = studentService.searchStudentsByName(keyword);
        return ResponseEntity.ok(ApiResponse.ok("Search results retrieved successfully", students));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Student>> createStudent(@Valid @RequestBody StudentRequest request) {
        Student created = studentService.createStudent(request);
        return new ResponseEntity<>(ApiResponse.ok("Student created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        Student updated = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Student updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.ok("Student deleted successfully with id: " + id, null));
    }

    // ==================== CÁC API GỌI QUERY TỪ ENTITY ====================

    // 1. Gọi NamedQuery "Student.findByDobBetween": Lọc sinh viên theo khoảng ngày sinh
    // Ví dụ: GET http://localhost:8080/api/students/filter-by-dob?start=2003-01-01&end=2003-12-31
    @GetMapping("/filter-by-dob")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByDobRange(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate start,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate end) {
        List<Student> students = studentService.getStudentsByDobRange(start, end);
        return ResponseEntity.ok(ApiResponse.ok("Students filtered by DOB range successfully", students));
    }

    // 2. Gọi NamedQuery "Student.findStudentsBornAfter": Tìm sinh viên sinh sau 1 ngày
    // Ví dụ: GET http://localhost:8080/api/students/born-after?dob=2003-01-01
    @GetMapping("/born-after")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsBornAfter(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dob) {
        List<Student> students = studentService.getStudentsBornAfter(dob);
        return ResponseEntity.ok(ApiResponse.ok("Students born after " + dob + " retrieved successfully", students));
    }

    // 3. Gọi NamedQuery "Student.findAllWithDepartmentOrdered": Lấy danh sách kèm Khoa có sắp xếp
    // Ví dụ: GET http://localhost:8080/api/students/ordered-by-dept
    @GetMapping("/ordered-by-dept")
    public ResponseEntity<ApiResponse<List<Student>>> getAllWithDepartmentOrdered() {
        List<Student> students = studentService.getAllWithDepartmentOrdered();
        return ResponseEntity.ok(ApiResponse.ok("Students ordered by department retrieved successfully", students));
    }

    // 4. Gọi NamedNativeQuery "Student.findStudentsByDeptNameNative": Native SQL query theo tên khoa
    // Ví dụ: GET http://localhost:8080/api/students/native-by-dept-name?deptName=Information Technology
    @GetMapping("/native-by-dept-name")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByDeptNameNative(@RequestParam String deptName) {
        List<Student> students = studentService.getStudentsByDeptNameNative(deptName);
        return ResponseEntity.ok(ApiResponse.ok("Students retrieved via Native SQL successfully", students));
    }

    // ==================== PHASE 1+2+3: SEARCH + PAGINATION + SORTING ====================

    /**
     * Advanced search, pagination, and sorting endpoint.
     * Delegates pagination, query/specification, and sorting to HibernateDao helpers.
     *
     * Search syntax: field{operator}value, comma-separated for AND.
     *   Operators: : (equality/contains), > (greater than), < (less than), ! (not equal)
     *   Allowed search fields: id, name, dob, department.name
     *
     * Pagination: page (0-based), size (default 10, max 100)
     * Sorting: sort=field,direction (e.g. sort=name,asc). Multiple sort params allowed.
     *   Allowed sort fields: id, name, dob
     *
     * Examples:
     *   GET /api/students/filter?search=name:Nguyen&page=0&size=10
     *   GET /api/students/filter?search=dob>2003-01-01&page=0&size=10&sort=name,asc
     *   GET /api/students/filter?search=name:Van,dob<2004-01-01&page=0&size=5&sort=dob,desc
     *   GET /api/students/filter?page=0&size=10&sort=name,asc  (no search = all students)
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PagedResponse<Student>>> filterStudents(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> sort) {

        // Build combined Pageable (Pagination + Sorting) using unified HibernateDao
        Pageable pageable = HibernateDao.createPageable(page, size, sort, ALLOWED_SORT_FIELDS);

        // Build Specification from search string using unified HibernateDao
        Specification<Student> spec = HibernateDao.parse(search, ALLOWED_SEARCH_FIELDS);

        // Execute query at database level
        Page<Student> resultPage = studentService.searchStudents(spec, pageable);

        // Wrap in PagedResponse
        PagedResponse<Student> pagedResponse = new PagedResponse<>(resultPage);

        return ResponseEntity.ok(ApiResponse.ok("Students filtered successfully", pagedResponse));
    }
}

