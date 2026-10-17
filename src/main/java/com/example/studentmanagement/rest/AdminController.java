package com.example.studentmanagement.rest;

import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.UserDto;
import com.example.studentmanagement.entity.RoleType;
import com.example.studentmanagement.entity.User;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
@Api(tags = {"Admin User Management & Role Assignment"})
public class AdminController {

    private final UserRepository userRepository;

    @Autowired
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    @ApiOperation(value = "List all user accounts (ADMIN only)")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved successfully", users));
    }

    @PutMapping("/users/{id}/roles")
    @ApiOperation(value = "Assign roles to user (ADMIN only)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRoles(
            @PathVariable Long id,
            @RequestBody Set<String> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Set<RoleType> newRoles = new HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                String normalized = r.trim().toUpperCase();
                if (!normalized.startsWith("ROLE_")) {
                    normalized = "ROLE_" + normalized;
                }
                try {
                    newRoles.add(RoleType.valueOf(normalized));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (newRoles.isEmpty()) {
            newRoles.add(RoleType.ROLE_STUDENT);
        }

        user.setRoles(newRoles);
        User updated = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("User roles updated successfully", new UserDto(updated)));
    }

    @PutMapping("/users/{id}/status")
    @ApiOperation(value = "Toggle user enabled status (ADMIN only)")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEnabled(enabled);
        User updated = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully", new UserDto(updated)));
    }
}
