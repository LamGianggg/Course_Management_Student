package com.example.studentmanagement.rest;

import com.example.studentmanagement.Dao.ISecurityTestTemplateDao;
import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.entity.SecurityTestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security-tests")
@CrossOrigin(origins = "*")
public class SecurityTestTemplateController {

    private final ISecurityTestTemplateDao securityTestTemplateDao;

    @Autowired
    public SecurityTestTemplateController(ISecurityTestTemplateDao securityTestTemplateDao) {
        this.securityTestTemplateDao = securityTestTemplateDao;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SecurityTestTemplate>>> getSecurityTestList(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "0") Integer lowerLimit,
            @RequestParam(required = false, defaultValue = "10") Integer upperLimit,
            @RequestParam(required = false, defaultValue = "id") String orderBy,
            @RequestParam(required = false, defaultValue = "asc") String orderType,
            @RequestParam(required = false) String appId,
            @RequestParam(required = false) String userAppId) {

        List<SecurityTestTemplate> list = securityTestTemplateDao.getSecurityTestList(
                query, lowerLimit, upperLimit, orderBy, orderType, appId, userAppId);
        return ResponseEntity.ok(ApiResponse.ok("Fetched security test list successfully", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SecurityTestTemplate>> create(@RequestBody SecurityTestTemplate template) {
        SecurityTestTemplate saved = securityTestTemplateDao.save(template);
        return ResponseEntity.ok(ApiResponse.ok("Created security test template successfully", saved));
    }
}
