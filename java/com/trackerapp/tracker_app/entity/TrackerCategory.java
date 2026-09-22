package com.trackerapp.tracker_app.entity;

/**
 * Represents the category of a tracker.
 * Predefined trackers use one of the fixed categories (STUDY, EXERCISE, HEALTH, HABIT, FINANCE).
 * User-created trackers that don't fit a predefined category use CUSTOM.
 */

public enum TrackerCategory {

    STUDY,
    EXERCISE,
    HEALTH,
    HABIT,
    FINANCE,
    CUSTOM
}
