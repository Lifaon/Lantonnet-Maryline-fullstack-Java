package com.paymybuddy.app.user;

import com.paymybuddy.app.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UserRepository repo;

    private final UserRelationRepository relationRepo;

    private final ApplicationContext context;

    private final JwtService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserService(UserRepository repo, UserRelationRepository relationRepo, ApplicationContext context, JwtService jwtService) {
        this.repo = repo;
        this.relationRepo = relationRepo;
        this.context = context;
        this.jwtService = jwtService;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(repo.findAll());
    }

    public Optional<User> getByOAuth(String provider, String id) {
        return repo.findByProviderAndProviderId(provider, id);
    }

    private static class UserNotFound extends ResponseStatusException {
        public UserNotFound() {
            super(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    public User getUserById(Long id) {
        return repo.findById(id).orElseThrow(UserNotFound::new);
    }

    public User getUserByUsername(String username) {
        return repo.findByUsername(username).orElseThrow(UserNotFound::new);
    }

    public User getUserByEmail(String email) {
        return repo.findByEmail(email).orElseThrow(UserNotFound::new);
    }

    public void createUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(encoder.encode(user.getPassword()));
        }
        repo.save(user);
        log.debug("User {} created", user.getId());
    }

    public String verify(User user) {
        Authentication authentication = context.getBean(AuthenticationManager.class).authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        if (!authentication.isAuthenticated()) {
            // TODO: throw
            return "Failure";
        }
        return jwtService.generateToken(user);
    }

    public void updateUser(User user) {
        if (!repo.existsById(user.getId())) {
            throw new UserNotFound();
        }
        repo.save(user);
        log.debug("User {} updated", user.getId());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = repo.findById(id).orElseThrow(UserNotFound::new);

        user.getRelations().clear();
        repo.save(user);

        List<UserRelation> relations = relationRepo.findAllByContact(user);
        relations.stream().map(UserRelation::getUser).forEach(owner -> owner.deleteRelation(user));
        relationRepo.deleteAll(relations);

        repo.delete(user);
        log.debug("User {} deleted", user.getId());
    }

    @Transactional
    public void deleteAllUsers() {
        List<User> users = repo.findAll();
        users.forEach(user -> user.getRelations().clear());
        repo.saveAll(users);
        repo.deleteAll();
        log.debug("All users deleted");
    }
}
