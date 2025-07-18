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
    private Set<UserContact> following = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserContact> followers = new HashSet<>();

    protected User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(Long id, String username, String email, String password, Set<UserContact> following, Set<UserContact> followers) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.following = following;
        this.followers = followers;
    }

    public UserDTO toDTO() {
        return new UserDTO(
                id,
                username,
                email,
                following.stream().map(UserContact::getContact).map(User::getId).toList()
        );
    }

    @Override
    public String toString() {
        return toDTO().toString();
    }

    public Set<UserContact> getFollowing() {
        return following;
    }

    public void setFollowing(Set<UserContact> following) {
        this.following = following;
    }

    public Set<UserContact> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<UserContact> followers) {
        this.followers = followers;
    }

    public void addContact(User contact) {
        following.add(new UserContact(this, contact));
    }

    public void removeContact(User contact) {
        following.removeIf(userContact -> userContact.getContact().equals(contact));
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
