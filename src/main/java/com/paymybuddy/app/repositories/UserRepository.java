package com.paymybuddy.app.repositories;

import com.paymybuddy.app.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findById(long id);
    User findByEmail(String email);
    User findByUsername(String username);
}
