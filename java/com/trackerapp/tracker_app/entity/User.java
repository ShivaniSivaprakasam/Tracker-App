package com.trackerapp.tracker_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a registered user of the Tracker application.
 * Stores authentication credentials, profile information, and owns
 * a collection of Trackers, Roles, and ActivityLog entries.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"password", "tracker"})
public class User extends BaseEntity{

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    // ==================== Relationships ====================

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // mappedBy: Tracker entity owns the FK; cascade ALL: deleting a User deletes their Trackers;
    // orphanRemoval: removing a Tracker from this list deletes it from DB too; LAZY: don't load all trackers by default
    private List<Tracker> trackers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ActivityLog> activityLogs = new ArrayList<>(); // deleting a user cascades to their activity history too

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = true; // legacy default true so pre-existing accounts aren't locked out; registerUser() explicitly sets this false for new signups

    /**
     * Computed (non-persisted) convenience check: true if this user
     * currently holds ROLE_ADMIN. Used by the admin panel to hide the
     * "Promote to Admin" action for users who are already admins.
     */
    @jakarta.persistence.Transient
    public boolean isAdmin() {
        return this.roles.stream().anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN);
    }

    @Column(name = "promoted_by")
    private Long promotedBy; // ID of the admin who promoted this user to ROLE_ADMIN; null for the seeded primary admin and for non-admin users

    @Column(name = "last_inactivity_nudge_sent")
    private LocalDate lastInactivityNudgeSent; // nullable — null means never nudged; used to enforce a cooldown between nudges
}
