package com.example.studentmanagement.rest;

import com.example.studentmanagement.dto.ClassInfoWrapper;
import com.example.studentmanagement.entity.ClassInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@Api(tags = {"ClassInfoRest"})
@SwaggerDefinition(tags = {@Tag(name = "ClassInfoRest", description = "REST APIs for Class Info module")})
public interface ClassInfoRest {

    // Tạo ClassInfo mới
    @PostMapping("/create")
    @ApiOperation(value = "create new Class Info", tags = {"create"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role"), @AuthorizationScope(scope = "ROLE_LECTURER", description = "Lecturer role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK"), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    ResponseEntity<ClassInfo> createClassInfo(@Valid @RequestBody ClassInfoWrapper wrapper);

    // Cập nhật ClassInfo
    @PutMapping("/update")
    @ApiOperation(value = "update existing Class Info", tags = {"update"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role"), @AuthorizationScope(scope = "ROLE_LECTURER", description = "Lecturer role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK"), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('LECTURER')")
    ResponseEntity<ClassInfo> updateClassInfo(@Valid @RequestBody ClassInfoWrapper wrapper);

    // Xóa ClassInfo
    @DeleteMapping("/{id}")
    @ApiOperation(value = "delete Class Info by ID", tags = {"delete"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK"), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<String> deleteClassInfo(@ApiParam(value = "Class Info ID", required = true) @PathVariable String id);

    // Lấy ClassInfo theo ID
    @GetMapping("/{id}")
    @ApiOperation(value = "get Class Info by ID", tags = {"getById"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_STUDENT", description = "Student role"), @AuthorizationScope(scope = "ROLE_LECTURER", description = "Lecturer role"), @AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK"), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER') or hasRole('ADMIN')")
    ResponseEntity<ClassInfo> getClassInfoById(@ApiParam(value = "Class Info ID", required = true) @PathVariable String id);

    // Lấy ClassInfo theo tên
    @GetMapping("/name/{name}")
    @ApiOperation(value = "get Class Info by name", tags = {"getByName"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_STUDENT", description = "Student role"), @AuthorizationScope(scope = "ROLE_LECTURER", description = "Lecturer role"), @AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK"), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER') or hasRole('ADMIN')")
    ResponseEntity<ClassInfo> getClassInfoByName(@ApiParam(value = "Class Info Name", required = true) @PathVariable String name);

    // Lấy tất cả ClassInfo
    @GetMapping("/list")
    @ApiOperation(value = "get all Class Info", tags = {"list"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_STUDENT", description = "Student role"), @AuthorizationScope(scope = "ROLE_LECTURER", description = "Lecturer role"), @AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK", response = List.class), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER') or hasRole('ADMIN')")
    ResponseEntity<List<ClassInfo>> getAllClassInfo();

    // Lấy ClassInfo theo domain
    @GetMapping("/domain/{domain}")
    @ApiOperation(value = "get Class Info by domain", tags = {"getByDomain"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_STUDENT", description = "Student role"), @AuthorizationScope(scope = "ROLE_LECTURER", description = "Lecturer role"), @AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK", response = List.class), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER') or hasRole('ADMIN')")
    ResponseEntity<List<ClassInfo>> getClassInfoByDomain(@ApiParam(value = "Domain", required = true) @PathVariable String domain);

    // Tìm kiếm ClassInfo
    @GetMapping("/search")
    @ApiOperation(value = "search Class Info", tags = {"search"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_STUDENT", description = "Student role"), @AuthorizationScope(scope = "ROLE_LECTURER", description = "Lecturer role"), @AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK", response = List.class), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER') or hasRole('ADMIN')")
    ResponseEntity<List<ClassInfo>> searchClassInfo(
            @ApiParam(value = "Query filter") @RequestParam(required = false) String query,
            @ApiParam(value = "Lower limit") @RequestParam(required = false) Integer lowerLimit,
            @ApiParam(value = "Upper limit") @RequestParam(required = false) Integer upperLimit,
            @ApiParam(value = "Order by field") @RequestParam(required = false) String orderBy,
            @ApiParam(value = "Order type (ASC/DESC)") @RequestParam(required = false) String orderType);

    // Đếm số lượng ClassInfo
    @GetMapping("/count")
    @ApiOperation(value = "get Class Info count", tags = {"count"}, authorizations = {@Authorization(value = "default", scopes = {@AuthorizationScope(scope = "ROLE_ADMIN", description = "Admin role")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success|OK"), @ApiResponse(code = 403, message = "Access is denied"), @ApiResponse(code = 404, message = "Not Found!!!")})
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Integer> getClassInfoCount(@ApiParam(value = "Query filter") @RequestParam(required = false) String query);
}
