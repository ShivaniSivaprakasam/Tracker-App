package com.trackerapp.tracker_app.repository;

import com.trackerapp.tracker_app.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 * Provides CRUD operations for Badge records earned by users.
 */

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long>{

    // Fetches all badges earned by a user — used on the profile/dashboard "trophy shelf".
    List<Badge> findByUserIdOrderByEarnedAtDesc(Long userId);
}
