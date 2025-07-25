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
    private final UserRelationRepository userRelationRepository;

    public UserService(UserRepository userRepository, UserRelationRepository userRelationRepository) {
        this.userRepository = userRepository;
        this.userRelationRepository = userRelationRepository;
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

        user.getRelations().clear();
        userRepository.save(user);

        List<UserRelation> relations = userRelationRepository.findAllByRelation(user);
        relations.stream().map(UserRelation::getUser).forEach(owner -> owner.removeRelation(user));
        userRelationRepository.deleteAll(relations);

        userRepository.delete(user);
    }

    @Transactional
    public void deleteAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> user.getRelations().clear());
        userRepository.saveAll(users);
        userRepository.deleteAll();
    }
}
