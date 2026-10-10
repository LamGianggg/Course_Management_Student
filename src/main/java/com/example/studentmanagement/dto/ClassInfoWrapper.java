package com.example.studentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClassInfoWrapper {

    private String id;

    @NotBlank(message = "Class name is required")
    @Size(min = 2, max = 100, message = "Class name must be between 2 and 100 characters")
    private String name;

    @Size(max = 100, message = "Domain cannot exceed 100 characters")
    private String domain;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    public ClassInfoWrapper() {
    }

    public ClassInfoWrapper(String name, String domain, String description) {
        this.name = name;
        this.domain = domain;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
