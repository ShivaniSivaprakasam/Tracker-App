package com.trackerapp.tracker_app.dto;

import com.trackerapp.tracker_app.entity.Tracker;
import com.trackerapp.tracker_app.entity.TrackerCategory;
import com.trackerapp.tracker_app.entity.TrackerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Read-only representation of a Tracker exposed over the REST API.
 * Deliberately excludes the User relationship and the progressEntries/
 * milestones collections — returning the entity directly would risk
 * lazy-loading exceptions outside a transaction and would leak internal
 * relationship structure that API clients don't need at this level.
 */
@Getter
@AllArgsConstructor
public class TrackerResponseDto {
    private Long id;
    private String name;
    private TrackerCategory category;
    private String progressLabel;
    private String purpose;
    private Double goalTarget;
    private Double currentProgress;
    private Double completionPercentage;
    private LocalDate deadline;
    private TrackerStatus status;
    private boolean favorite;
    private Integer currentStreak;
    private Integer longestStreak;
    private Long daysRemaining;
    private boolean expired;

    public static TrackerResponseDto fromEntity(Tracker tracker) {
        return new TrackerResponseDto(
                tracker.getId(),
                tracker.getName(),
                tracker.getCategory(),
                tracker.getProgressLabel(),
                tracker.getPurpose(),
                tracker.getGoalTarget(),
                tracker.getCurrentProgress(),
                tracker.getCompletionPercentage(),
                tracker.getDeadline(),
                tracker.getStatus(),
                tracker.isFavorite(),
                tracker.getCurrentStreak(),
                tracker.getLongestStreak(),
                tracker.getDaysRemaining(),
                tracker.isExpired()
        );
    }
}