package com.trackerapp.tracker_app.entity;

/**
 * Enumerates the kinds of actions recorded in the ActivityLog, as
 * specified in the project requirements (registration, profile updates,
 * password changes, tracker lifecycle events, goal completion).
 */

public enum ActivityType {

    REGISTRATION,
    PROFILE_UPDATED,
    PASSWORD_CHANGED,
    TRACKER_CREATED,
    TRACKER_UPDATED,
    GOAL_COMPLETED,
    TRACKER_ARCHIVED,
    TRACKER_RESTORED
}
