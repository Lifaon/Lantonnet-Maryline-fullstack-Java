package com.paymybuddy.app.userconnection;

import com.paymybuddy.app.user.User;
import org.springframework.data.repository.CrudRepository;

public interface UserConnectionRepository extends CrudRepository<UserConnection, Long> {
    UserConnection findByUser(User id);
    UserConnection findByConnection(User id);
}
