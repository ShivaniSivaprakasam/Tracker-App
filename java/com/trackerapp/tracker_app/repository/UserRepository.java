package com.trackerapp.tracker_app.repository;


import com.trackerapp.tracker_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Provides CRUD operations for User entities plus custom lookup methods
 * needed for authentication (finding by email) and registration
 * (checking for duplicate emails).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    // Spring Data JPA auto-implements this from the method name — no query needed.
    // Used during login to fetch a user by their email for authentication.
    Optional<User> findByEmail(String email);

    // Used during registration to reject duplicate email addresses before saving.
    boolean existsByEmail(String email);
}
