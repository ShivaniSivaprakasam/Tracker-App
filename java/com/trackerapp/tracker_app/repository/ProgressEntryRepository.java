package com.trackerapp.tracker_app.repository;

import com.trackerapp.tracker_app.entity.ProgressEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

/**
 * Provides CRUD operations for ProgressEntry records, which together
 * form the full progress history of a Tracker.
 */

@Repository
public interface ProgressEntryRepository extends JpaRepository<ProgressEntry, Long>{

    // Fetches all progress entries for a tracker, most recent first — used on the Progress History page.
    List<ProgressEntry> findByTrackerIdOrderByEntryDateDesc(Long trackerId);

    Optional<ProgressEntry> findTopByTracker_UserIdOrderByEntryDateDesc(Long userId);
}
