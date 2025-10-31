package com.paymybuddy.app.user;

import com.paymybuddy.app.security.LoginForm;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByEmail(String email);
    List<User> findAllByUsername(String username);

    Optional<User> findFirstByEmail(String email);
    Optional<User> findFirstByUsername(String username);

    Optional<User> findByProviderAndProviderId(String provider, String id);
    Optional<User> findByEmailAndProvider(String email, String provider);
}
