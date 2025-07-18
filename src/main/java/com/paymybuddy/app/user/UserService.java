package com.paymybuddy.app.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userRepository.findAll());
    }

    private static class UserNotFound extends ResponseStatusException {
        public UserNotFound() {
            super(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFound::new);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(UserNotFound::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFound::new);
    }

    public void addUser(User user) {
        userRepository.save(user);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFound::new);

        user.getFollowers().stream().map(UserContact::getUser).forEach(follower ->
                follower.getFollowing().removeIf(e -> e.getContact().equals(user)));

        user.getFollowing().stream().map(UserContact::getContact).forEach(following ->
                following.getFollowers().removeIf(e -> e.getUser().equals(user)));

        userRepository.delete(user);
    }

    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
