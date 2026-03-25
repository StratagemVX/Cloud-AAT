package com.chatapp.repository;

import com.chatapp.model.User;
import com.chatapp.model.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by their unique username. */
    Optional<User> findByUsername(String username);

    /** Check whether a username is already taken. */
    boolean existsByUsername(String username);

    /** Return all users whose status matches the given value. */
    List<User> findByStatus(UserStatus status);
}
