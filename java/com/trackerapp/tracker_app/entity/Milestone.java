package com.trackerapp.tracker_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.domain.Auditable;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents an achievement level detected for a Tracker (e.g. "50% Complete",
 * "10-Day Streak"). Milestones are tracker-specific — they describe *what*
 * was achieved and *when*; the corresponding reward (if any) is represented
 * separately by a Badge awarded to the User.
 */

@Entity
@Table(name = "milestones")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "tracker")
public class Milestone extends BaseEntity {

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "descriptioon", length = 255)
    private String description;

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracker_id", nullable = false) // FK linking this milestone to its tracker
    private Tracker tracker; // the tracker this milestone was achieved on
}
