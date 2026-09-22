package com.trackerapp.tracker_app.repository;

import com.trackerapp.tracker_app.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Provides CRUD operations for Milestone records achieved on a Tracker.
 */

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long>{

    // Fetches all milestones for a given tracker, most recent first — used to display milestone history.
    List<Milestone> findByTrackerIdOrderByAchievedAtDesc(Long trackerId);
}
