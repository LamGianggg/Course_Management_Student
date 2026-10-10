package com.example.studentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "APP_DETAIL")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AppDetail {

    @Id
    @Column(name = "ID", nullable = false, length = 64)
    private String id;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "CREATED_TIME")
    private LocalDateTime createdTime;

    public AppDetail() {
    }

    public AppDetail(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdTime = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdTime == null) {
            this.createdTime = LocalDateTime.now();
        }
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
