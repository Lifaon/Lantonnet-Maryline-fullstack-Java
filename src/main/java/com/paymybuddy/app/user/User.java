package com.paymybuddy.app.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "user")
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRelation> relations = new HashSet<>();

    protected User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(Long id, String username, String email, String password, Set<UserRelation> relations) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.relations = relations;
    }

    public UserDTO toDTO() {
        return new UserDTO(
                id,
                username,
                email,
                relations.stream().map(UserRelation::getRelation).map(User::getId).toList()
        );
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

    public void addRelation(User relation) {
        relations.add(new UserRelation(this, relation));
    }

    public void removeRelation(User relation) {
        relations.removeIf(userRelation -> userRelation.getRelation().equals(relation));
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
}
