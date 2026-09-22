package com.trackerapp.tracker_app.service;

import com.trackerapp.tracker_app.dto.ProgressEntryDto;
import com.trackerapp.tracker_app.dto.TrackerCreateDto;
import com.trackerapp.tracker_app.entity.Milestone;
import com.trackerapp.tracker_app.entity.ProgressEntry;
import com.trackerapp.tracker_app.entity.Tracker;
import com.trackerapp.tracker_app.entity.TrackerCategory;
import com.trackerapp.tracker_app.entity.TrackerStatus;

import java.util.List;

public interface TrackerService {

    List<Tracker> getActiveTrackersForUser(Long userId);

    List<Tracker> getFavoriteTrackersForUser(Long userId);

    DashboardStats getDashboardStats(Long userId);

    MonthlySummary getMonthlySummary(Long userId, int year, int month);

    Tracker createTracker(Long userId, TrackerCreateDto dto);

    Tracker getTrackerById(Long trackerId);

    void updateTracker(Long trackerId, TrackerCreateDto dto);

    void logProgress(Long trackerId, ProgressEntryDto dto);

    List<ProgressEntry> getProgressHistory(Long trackerId);

    List<Milestone> getMilestonesForTracker(Long trackerId);

    void toggleFavorite(Long trackerId);

    Tracker duplicateTracker(Long trackerId);

    void archiveTracker(Long trackerId);

    void restoreTracker(Long trackerId);

    void deleteTracker(Long trackerId);

    List<Tracker> getArchivedTrackersForUser(Long userId);

    /**
     * Returns the user's trackers filtered by an optional case-insensitive
     * name search, optional category, and optional status, then sorted by
     * sortBy ("name", "oldest", "goal", or default: most recently updated
     * first). Any null filter is simply skipped.
     */
    List<Tracker> searchSortFilterTrackers(Long userId, String search, TrackerCategory category, TrackerStatus status, String sortBy);

    String buildCsvExport(Long trackerId);

    byte[] buildPdfExport(Long trackerId);

    record DashboardStats(
            long totalTrackers,
            long activeTrackers,
            long archivedTrackers,
            long goalsCompleted,
            long totalBadgesEarned,
            int bestCurrentStreak
    ) {}

    record MonthlySummary(
            long goalsCompletedThisMonth,
            long goalsMissedThisMonth,
            String bestPerformingTrackerName,
            int longestStreakThisMonth
    ) {}
    /**
     * Fetches a tracker and verifies it belongs to the given user in one
     * step — the single entry point every controller endpoint should use
     * instead of calling getTrackerById() directly, to prevent one user
     * from accessing or modifying another user's tracker by guessing IDs
     * (IDOR/BOLA protection).
     *
     * @throws org.springframework.security.access.AccessDeniedException if the tracker belongs to a different user
     */
    Tracker getTrackerForUser(Long trackerId, Long userId);

    void trashTracker(Long trackerId);

    List<Tracker> getTrashedTrackersForUser(Long userId);

    org.springframework.data.domain.Page<Tracker> getActiveTrackersPaged(Long userId, org.springframework.data.domain.Pageable pageable);

    record CalendarEvent(java.time.LocalDate date, String label, Long trackerId, String type) {}

    List<CalendarEvent> getCalendarEventsForMonth(Long userId, int year, int month);
}