package com.paymybuddy.app.user;

import java.util.List;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private List<Long> contactIds;

    public UserDTO(Long id, String username, String email, List<Long> contactIds) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.contactIds = contactIds;
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

    public List<Long> getContactIds() {
        return contactIds;
    }

    public void setContactIds(List<Long> contactIds) {
        this.contactIds = contactIds;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", contactIds=" + contactIds +
                '}';
    }
}
