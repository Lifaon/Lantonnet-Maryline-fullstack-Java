package com.paymybuddy.app.user;

import java.util.List;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private List<Long> relationIds;
    private List<Long> bankAccountIds;

    public UserDTO(Long id, String username, String email, List<Long> relationIds, List<Long> bankAccountIds) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.relationIds = relationIds;
        this.bankAccountIds = bankAccountIds;
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

    public List<Long> getRelationIds() {
        return relationIds;
    }

    public void setRelationIds(List<Long> relationIds) {
        this.relationIds = relationIds;
    }

    public List<Long> getBankAccountIds() {
        return bankAccountIds;
    }

    public void setBankAccountIds(List<Long> bankAccountIds) {
        this.bankAccountIds = bankAccountIds;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", relationIds=" + relationIds +
                ", bankAccountIds=" + bankAccountIds +
                '}';
    }
}
