package com.trackerapp.tracker_app.repository;

import com.trackerapp.tracker_app.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Provides CRUD operations for ActivityLog records. This is an append-only
 * audit trail, so no update methods are needed beyond the inherited save().
 */

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>{

    // Fetches all activity for a user in reverse chronological order — used on the activity log page.
    List<ActivityLog> findByUserIdOrderByCreatedDateDesc(Long userId);
}
