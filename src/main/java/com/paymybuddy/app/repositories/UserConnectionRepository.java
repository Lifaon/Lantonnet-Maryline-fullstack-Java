package com.paymybuddy.app.repositories;

import com.paymybuddy.app.models.User;
import com.paymybuddy.app.models.UserConnection;
import org.springframework.data.repository.CrudRepository;

public interface UserConnectionRepository extends CrudRepository<UserConnection, Long> {
    UserConnection findByUser(User id);
    UserConnection findByConnection(User id);
}
