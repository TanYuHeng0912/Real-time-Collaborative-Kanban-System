package com.kanban.repository;

import com.kanban.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsDeletedFalse(String username);
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Boolean existsByUsernameAndIsDeletedFalse(String username);
    Boolean existsByEmailAndIsDeletedFalse(String email);
}

