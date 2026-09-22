package com.trackerapp.tracker_app.repository;

import com.trackerapp.tracker_app.entity.Tracker;
import com.trackerapp.tracker_app.entity.TrackerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Provides CRUD operations for Tracker entities plus query methods
 * supporting the dashboard, search, sort, and filter features.
 */

@Repository
public interface TrackerRepository extends JpaRepository<Tracker, Long>{

    // Fetches all trackers belonging to a specific user (used on the dashboard).
    List<Tracker> findByUserId(Long userId);

    // Fetches only trackers of a given status for a user (ACTIVE / ARCHIVED / COMPLETED) — powers dashboard filtering.
    List<Tracker> findByUserIdAndStatus(Long userId, TrackerStatus status);

    // Fetches favorite trackers for a user — used to show them at the top of the dashboard.
    List<Tracker> findByUserIdAndFavoriteTrue(Long userId);

    // Case-insensitive partial name search — powers the Search Trackers feature.
    List<Tracker> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    org.springframework.data.domain.Page<Tracker> findByUserIdAndStatus(
            Long userId, TrackerStatus status, org.springframework.data.domain.Pageable pageable);

    List<Tracker> findByStatusAndDeadlineAndDeadlineReminderSentFalse(TrackerStatus status, LocalDate deadline);
}
