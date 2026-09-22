package com.trackerapp.tracker_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Represents a reward badge earned by a User for reaching a Milestone.
 * Kept separate from Milestone so a user's full badge collection can be
 * queried directly (e.g. for a "trophy shelf" view) without joining
 * through every individual tracker.
 */

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "user")
public class Badge extends BaseEntity{

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK linking this badge to the user who earned it
    private User user; // the user who owns this badge
}
