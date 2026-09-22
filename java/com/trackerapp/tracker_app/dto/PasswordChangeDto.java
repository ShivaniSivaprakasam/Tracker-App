package com.trackerapp.tracker_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Binds the "change password" form. Requires the current password for
 * re-verification (standard security practice — prevents someone who
 * hijacks an already-open session from silently locking out the real
 * owner) plus a new password entered twice to catch typos.
 */

@Data
public class PasswordChangeDto {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Please confirm your new password")
    private String confirmNewPassword;
}
