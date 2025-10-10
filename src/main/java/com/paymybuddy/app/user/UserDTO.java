package com.paymybuddy.app.user;

import java.util.List;
import java.util.Set;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Set<UserRole> roles;
    private String provider;
    private List<Long> relationIds;
    private Long bankAccountId;

    public UserDTO(Long id, String username, String email, Set<UserRole> roles, String provider, List<Long> relationIds, Long bankAccountId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.provider = provider;
        this.relationIds = relationIds;
        this.bankAccountId = bankAccountId;
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

    public Long getBankAccountIds() {
        return bankAccountId;
    }

    public void setBankAccountIds(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", provider='" + provider + '\'' +
                ", relationIds=" + relationIds +
                ", bankAccountId=" + bankAccountId +
                '}';
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }
}
