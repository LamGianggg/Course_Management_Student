package com.example.studentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_info")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NamedQueries({
    @NamedQuery(
        name = "ClassInfo.findByName",
        query = "SELECT c FROM ClassInfo c WHERE c.name = :name"
    ),
    @NamedQuery(
        name = "ClassInfo.findByDomain",
        query = "SELECT c FROM ClassInfo c WHERE c.domain = :domain ORDER BY c.createdTime DESC"
    ),
    @NamedQuery(
        name = "ClassInfo.findAllOrderByCreatedTimeDesc",
        query = "SELECT c FROM ClassInfo c ORDER BY c.createdTime DESC"
    )
})
public class ClassInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "domain", length = 100)
    private String domain;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    public ClassInfo() {
    }

    public ClassInfo(String id, String name, String domain, String description, LocalDateTime createdTime) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.description = description;
        this.createdTime = createdTime;
    }

    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
