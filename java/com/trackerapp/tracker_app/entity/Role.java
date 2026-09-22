package com.trackerapp.tracker_app.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a role that can be assigned to a User (e.g., ROLE_USER, ROLE_ADMIN).
 * Modeled as a separate entity (rather than a plain string on User) to support
 * a clean Many-to-Many relationship and future extensibility (e.g., adding
 * permissions per role without restructuring the User table).
 */

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role  extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private RoleName name;
}
