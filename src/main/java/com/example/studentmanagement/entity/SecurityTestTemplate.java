package com.example.studentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;

@Entity
@Table(name = "SECURITY_TEST_TEMPLATE")
@FilterDef(name = "filterTestByUserAppId", parameters = @ParamDef(name = "userAppId", type = String.class))
@Filter(name = "filterTestByUserAppId", condition = "USER_APP_ID = :userAppId")
@FilterDef(name = "filterTestByApplicationId", parameters = @ParamDef(name = "appId", type = String.class))
@Filter(name = "filterTestByApplicationId", condition = "APP_ID = :appId")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SecurityTestTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "APP_ID", length = 64)
    private String appId;

    @Column(name = "USER_APP_ID", length = 64)
    private String userAppId;

    @Column(name = "TEST_TYPE", length = 50)
    private String testType;

    @Column(name = "STATUS", length = 50)
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "CREATED_TIME")
    private LocalDateTime createdTime = LocalDateTime.now();

    public SecurityTestTemplate() {
    }

    public SecurityTestTemplate(Long id, String name, String description, String appId, String userAppId, String testType, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.appId = appId;
        this.userAppId = userAppId;
        this.testType = testType;
        this.status = status;
        this.createdTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserAppId() {
        return userAppId;
    }

    public void setUserAppId(String userAppId) {
        this.userAppId = userAppId;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
