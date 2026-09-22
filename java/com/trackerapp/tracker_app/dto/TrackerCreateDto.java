package com.trackerapp.tracker_app.dto;

import com.trackerapp.tracker_app.entity.TrackerCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Binds the tracker creation form for BOTH predefined and custom trackers —
 * per the project spec, both paths use identical backend logic and the
 * same underlying Tracker entity. For predefined trackers, the controller
 * pre-fills name/progressLabel/category before displaying the form; for
 * custom trackers, these start blank and the user fills everything in.
 */
@Data
public class TrackerCreateDto {

    @NotBlank(message = "Tracker name is required")
    @Size(max = 150, message = "Tracker name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Category is required")
    private TrackerCategory category;

    @Size(max = 100, message = "Progress label must not exceed 100 characters")
    private String progressLabel; // e.g. "Books Read" — required in practice for CUSTOM, pre-filled for predefined

    @Size(max = 255, message = "Purpose must not exceed 255 characters")
    private String purpose; // optional free-text description

    @NotNull(message = "Goal target is required")
    @Positive(message = "Goal target must be a positive number")
    private Double goalTarget;

    @jakarta.validation.constraints.FutureOrPresent(message = "Deadline cannot be in the past")
    private LocalDate deadline; // optional
}