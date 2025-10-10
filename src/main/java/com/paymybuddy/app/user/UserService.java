package com.paymybuddy.app.user;

import com.paymybuddy.app.bankaccounts.BankAccount;
import com.paymybuddy.app.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

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

    public String verify(UserDetails userDetails) {
        Authentication authentication = context.getBean(AuthenticationManager.class).authenticate(
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword()));
        // Generate new details from logged user, to specify the id instead of the username
        long id;
        try {
            id = Long.parseLong(authentication.getName());
        }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        User user = repo.findById(id).orElseThrow(UserNotFound::new);
        return jwtService.generateToken(user.toUserDetails());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getUserByEmail(email).toUserDetails();
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

    public User getLoggedUser() {
        return repo.findById(JwtService.getLoggedUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public List<UserRelation> getLoggedUserRelations() {
        return relationRepo.findAllByUserId(JwtService.getLoggedUserId());
    }

    public void createUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(encoder.encode(user.getPassword()));
        }
        if (user.getBankAccount() == null) {
            user.setBankAccount(new BankAccount(user, 0.));
        }
        repo.save(user);
        log.debug("User {} created", user.getId());
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
