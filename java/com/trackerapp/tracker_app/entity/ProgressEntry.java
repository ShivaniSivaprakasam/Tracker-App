package com.trackerapp.tracker_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * Represents a single progress update recorded against a Tracker.
 * Kept as its own table (rather than a field on Tracker) so users can
 * accumulate a full history of updates over time, each with its own
 * date, value, and optional note.
 */

@Entity
@Table(name = "progress_entries")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "trakcer")
public class ProgressEntry  extends BaseEntity {

    @NotNull(message = "Progress value is required")
    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "entry_date", nullable = false)
    private LocalDate  entryDate;

    @Column(name = "notes", length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: we don't need the full Tracker object every time we list entries
    @JoinColumn(name = "tracker_id", nullable = false) // FK linking this entry back to its parent Tracker
    private Tracker tracker; // the tracker this progress update belongs to
}
