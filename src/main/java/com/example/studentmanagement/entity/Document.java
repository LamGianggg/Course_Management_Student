package com.example.studentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "DOCUMENT")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Document {

    @Id
    @Column(name = "ID", updatable = false, nullable = false, length = 64)
    private String id;

    @Column(name = "APP_DETAIL_FK", nullable = false, length = 64)
    private String appId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "PATH", nullable = false, length = 500)
    private String path;

    @Column(name = "OWNER", length = 100)
    private String owner;

    @Column(name = "VERSION")
    private Double version;

    @Column(name = "DELETED")
    private Boolean deleted;

    @Column(name = "CREATED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creationTime;

    @Column(name = "MODIFIED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modificationTime;

    @Column(name = "ACTIVE")
    private Boolean active;

    public Document() {
    }

    public Document(String id, String appId, String name, String path, String owner,
                    Double version, Boolean deleted, Date creationTime, Date modificationTime, Boolean active) {
        this.id = id;
        this.appId = appId;
        this.name = name;
        this.path = path;
        this.owner = owner;
        this.version = version;
        this.deleted = deleted;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
        this.active = active;
    }

    @PrePersist
    public void onCreate() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
        Date now = new Date();
        if (this.creationTime == null) {
            this.creationTime = now;
        }
        if (this.modificationTime == null) {
            this.modificationTime = now;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
        if (this.active == null) {
            this.active = true;
        }
        if (this.version == null) {
            this.version = 1.0;
        }
    }

    @PreUpdate
    public void onPersist() {
        this.modificationTime = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
