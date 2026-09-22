package com.trackerapp.tracker_app.repository;

import com.trackerapp.tracker_app.entity.Role;
import com.trackerapp.tracker_app.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Provides CRUD operations for Role entities. Roles are seeded once at
 * startup (ROLE_USER, ROLE_ADMIN) and rarely modified afterward, so this
 * repository stays minimal.
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

    // Used when assigning a role to a new user during registration/admin creation.
    Optional<Role> findByName(RoleName name);
}
