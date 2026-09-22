package com.trackerapp.tracker_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single tracker owned by a user — either created from a
 * predefined template (STUDY, EXERCISE, HEALTH, HABIT, FINANCE) or fully
 * custom (CUSTOM category). Both paths use this same entity and the same
 * backend logic, per the project spec; only the meaning of "progress"
 * changes depending on category.
 */
@Entity
@Table(name = "trackers")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "progressEntries", "milestones"}) // avoid huge/circular output
public class Tracker extends BaseEntity {

    @NotBlank(message = "Tracker name is required")
    @Column(name = "name", nullable = false, length = 150)
    private String name; // e.g. "Read 12 Books This Year"

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private TrackerCategory category; // STUDY / EXERCISE / HEALTH / HABIT / FINANCE / CUSTOM

    @Column(name = "progress_label", length = 100)
    private String progressLabel; // e.g. "Books Read", "Plants Watered" — only meaningful for CUSTOM trackers

    @Column(name = "purpose", length = 255)
    private String purpose; // free-text description of what this tracker is for

    // ==================== Goal & Progress ====================

    @NotNull(message = "Goal target value is required")
    @Column(name = "goal_target", nullable = false)
    private Double goalTarget; // the measurable target, e.g. 12 (books), 50 (kg), 30 (days)

    @Column(name = "current_progress", nullable = false)
    private Double currentProgress = 0.0; // running total, updated whenever a ProgressEntry is added

    @Column(name = "completion_percentage", nullable = false)
    private Double completionPercentage = 0.0; // derived value: (currentProgress / goalTarget) * 100, recalculated on update

    @Column(name = "deadline")
    private LocalDate deadline; // optional — nullable since not every tracker needs a hard deadline

    // ==================== Status Tracking ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TrackerStatus status = TrackerStatus.ACTIVE; // ACTIVE / COMPLETED / ARCHIVED — defaults to ACTIVE on creation

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false; // marks whether the user has pinned this tracker to the top of the dashboard

    // ==================== Streak Tracking ====================

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0; // consecutive successful update periods (e.g. days)

    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0; // historical best streak, kept even if currentStreak resets

    @Column(name = "last_progress_date")
    private LocalDate lastProgressDate; // used to calculate whether the streak continues or resets on next update

    // ==================== Relationships ====================

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: we rarely need the full User object just from a Tracker
    @JoinColumn(name = "user_id", nullable = false) // FK column linking this tracker to its owner
    private User user; // the owner of this tracker

    @OneToMany(mappedBy = "tracker", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProgressEntry> progressEntries = new ArrayList<>(); // full history of progress updates for this tracker

    @OneToMany(mappedBy = "tracker", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Milestone> milestones = new ArrayList<>(); // achievements detected/reached for this tracker

    /**
     * Computed (non-persisted) property: number of days remaining until
     * the deadline, or null if no deadline is set. Negative values mean
     * the deadline has passed. Calculated on-the-fly rather than stored,
     * so it's always accurate without needing a scheduled background job.
     */
    @Transient
    public Long getDaysRemaining() {
        if (this.deadline == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), this.deadline);
    }

    /**
     * Computed (non-persisted) property: true if this tracker has a
     * deadline that has already passed and the tracker is not yet completed.
     */
    @Transient
    public boolean isExpired() {
        if (this.deadline == null) {
            return false;
        }
        return this.deadline.isBefore(java.time.LocalDate.now()) && this.status != TrackerStatus.COMPLETED;
    }

    /**
     * Computed (non-persisted) property: true if this tracker was
     * completed on or before its deadline (used to show "completed on time"
     * vs "completed late" messaging).
     */
    @Transient
    public boolean isCompletedOnTime() {
        if (this.deadline == null || this.status != TrackerStatus.COMPLETED) {
            return false;
        }
        return !this.getUpdatedDate().toLocalDate().isAfter(this.deadline);
    }

    @Column(name = "deadline_reminder_sent", nullable = false)
    private boolean deadlineReminderSent = false; // prevents re-sending the "3 days to go" email daily until the deadline passes
}