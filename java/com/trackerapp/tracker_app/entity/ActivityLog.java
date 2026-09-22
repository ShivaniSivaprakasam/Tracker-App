package com.trackerapp.tracker_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Append-only audit trail of significant account actions (registration,
 * profile updates, tracker creation/archiving, goal completion, etc.).
 * Used to power the "Account Activity Log" feature so users can review
 * a chronological history of what happened on their account.
 */

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "user")
public class ActivityLog extends BaseEntity{

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActivityType actionType;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK linking this log entry to the user who performed the action
    private User user; // the user this activity belongs to

    // Note: createdDate (inherited from BaseEntity) doubles as the timestamp
    // for when this activity occurred — no separate field needed.
}
