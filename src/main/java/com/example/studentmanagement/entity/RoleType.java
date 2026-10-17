package com.example.studentmanagement.entity;

public enum RoleType {
    ROLE_ADMIN,
    ROLE_LECTURER,
    ROLE_STUDENT;

    public String getCleanName() {
        return name().replace("ROLE_", "");
    }
}
