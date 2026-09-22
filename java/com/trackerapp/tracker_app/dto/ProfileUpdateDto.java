package com.trackerapp.tracker_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


/**
 * Binds the "edit profile" form. Deliberately excludes email and password —
 * email changes and password changes are handled through separate,
 * more carefully validated flows (email uniqueness, current-password
 * verification) rather than a generic profile edit.
 */

@Data
public class ProfileUpdateDto {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @jakarta.validation.constraints.Pattern(
            regexp = "^$|^(\\+91|91)?[6-9]\\d{9}$",
            message = "Enter a valid 10-digit Indian mobile number (optional prefixed with +91)"
    )
    private String phoneNumber; // optional — left blank counts against profile completion %

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Size(max = 255, message = "Profile picture URL must nor exceed 255 characters")
    private String profilePictureUrl; // optional — a URL/path, not a file upload, at this stage
}
