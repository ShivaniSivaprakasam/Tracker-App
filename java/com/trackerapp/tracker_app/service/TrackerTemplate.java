package com.trackerapp.tracker_app.service;

import com.trackerapp.tracker_app.dto.TrackerCreateDto;
import com.trackerapp.tracker_app.entity.TrackerCategory;

/**
 * Defines the fixed set of predefined tracker templates. These are
 * code-level constants (not database rows) since they represent the
 * application's built-in defaults, not user-editable data. Each template
 * provides sensible starting values that get pre-filled into a
 * TrackerCreateDto before the user reviews and confirms them.
 */
public final class TrackerTemplate {

    private TrackerTemplate() {
        // Utility class — not meant to be instantiated
    }

    /**
     * Builds a pre-filled TrackerCreateDto for the given predefined
     * category, with sensible default name, progress label, and goal.
     * The user can still adjust these values before saving.
     */
    public static TrackerCreateDto buildDefault(TrackerCategory category) {
        TrackerCreateDto dto = new TrackerCreateDto();
        dto.setCategory(category);

        switch (category) {
            case STUDY -> {
                dto.setName("Study Tracker");
                dto.setProgressLabel("Hours Studied");
                dto.setPurpose("Track hours spent studying toward a learning goal");
                dto.setGoalTarget(100.0);
            }
            case EXERCISE -> {
                dto.setName("Exercise Tracker");
                dto.setProgressLabel("Workouts Completed");
                dto.setPurpose("Track workout sessions toward a fitness goal");
                dto.setGoalTarget(30.0);
            }
            case HEALTH -> {
                dto.setName("Health Tracker");
                dto.setProgressLabel("Weight Lost (kg)");
                dto.setPurpose("Track progress toward a health or weight goal");
                dto.setGoalTarget(10.0);
            }
            case HABIT -> {
                dto.setName("Habit Tracker");
                dto.setProgressLabel("Days Completed");
                dto.setPurpose("Build a new habit through consistent daily action");
                dto.setGoalTarget(30.0);
            }
            case FINANCE -> {
                dto.setName("Finance Tracker");
                dto.setProgressLabel("Amount Saved");
                dto.setPurpose("Track savings progress toward a financial goal");
                dto.setGoalTarget(1000.0);
            }
            default -> throw new IllegalArgumentException("No predefined template exists for category: " + category);
        }

        return dto;
    }
}