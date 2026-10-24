package com.example.studentmanagement.dto;

public class UserInfoWrapper {

    private Long id;
    private String username;
    private String email;
    private String organisation;
    private String status;

    public UserInfoWrapper() {
    }

    public UserInfoWrapper(Long id, String username, String email, String organisation, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.organisation = organisation;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserInfoWrapper{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", organisation='" + organisation + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
