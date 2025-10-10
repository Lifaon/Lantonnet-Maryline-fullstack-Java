package com.paymybuddy.app.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRelationRepository extends JpaRepository<UserRelation, Long> {

    List<UserRelation> findAllByUser(User user);
    List<UserRelation> findAllByUserId(Long userId);

    List<UserRelation> findAllByContact(User contact);
}
