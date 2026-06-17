package com.sms.auth.repository;

import com.sms.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 * All CRUD methods are inherited from JpaRepository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email (used during login and JWT validation).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email is already registered (used during registration).
     */
    boolean existsByEmail(String email);
}
