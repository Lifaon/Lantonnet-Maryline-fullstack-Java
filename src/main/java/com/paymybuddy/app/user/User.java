package com.paymybuddy.app.user;

import com.paymybuddy.app.bankaccounts.BankAccount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity(name = "user_list")
@Table(name = "user_list")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String provider;

    @Column
    private String providerId;

    @Column
    private String password;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = Set.of(UserRole.USER);

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRelation> relations = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private BankAccount bankAccount;

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.provider = "local";
        this.password = password;
    }

    public User(Long id, String username, String email, String provider, String providerId, String password, Set<UserRole> roles, Set<UserRelation> relations, BankAccount bankAccount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.password = password;
        this.roles = roles;
        this.relations = relations;
        this.bankAccount = bankAccount;
    }

    public UserDTO toDTO() {
        return new UserDTO(
                id,
                username,
                email,
                roles,
                provider,
                relations.stream().map(UserRelation::getContact).map(User::getId).toList(),
                bankAccount == null ? null : bankAccount.getId()
        );
    }

    public UserDetails toUserDetails() {
        return org.springframework.security.core.userdetails.User.builder()
                .username(id.toString())
                .password(password != null ? password : "")
                .authorities(roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public String toString() {
        return toDTO().toString();
    }

    public Set<UserRelation> getRelations() {
        return relations;
    }

    public void setRelations(Set<UserRelation> relations) {
        this.relations = relations;
    }

    public void createRelation(User contact) {
        relations.add(new UserRelation(this, contact));
    }

    public void deleteRelation(User contact) {
        relations.removeIf(userRelation -> userRelation.getContact().equals(contact));
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }
}
