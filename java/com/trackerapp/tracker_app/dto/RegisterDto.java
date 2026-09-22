package com.trackerapp.tracker_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object used to bind the registration form. Deliberately
 * decoupled from the User entity — a form should never be able to set
 * fields like roles, enabled, or id directly. Only what the user actually
 * fills in belongs here.
 */

@Data
public class RegisterDto {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must not be below 3 characters and must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a vlid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
