package com.example.studentmanagement.rest;

import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.UserInfoWrapper;
import com.example.studentmanagement.entity.UserInfo;
import com.example.studentmanagement.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user-info")
@CrossOrigin(origins = "*")
public class UserInfoController {

    private final IUserInfoService userInfoService;

    @Autowired
    public UserInfoController(IUserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Map<String, String>>> update(@RequestBody UserInfoWrapper userInfoWrapper) {
        Map<String, String> result = userInfoService.update(userInfoWrapper);
        return ResponseEntity.ok(ApiResponse.ok("User info updated successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserInfo>> create(@RequestBody UserInfo userInfo) {
        UserInfo saved = userInfoService.create(userInfo);
        return ResponseEntity.ok(ApiResponse.ok("User info created successfully", saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfo>> getById(@PathVariable Long id) {
        UserInfo userInfo = userInfoService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok("User info retrieved successfully", userInfo));
    }
}
