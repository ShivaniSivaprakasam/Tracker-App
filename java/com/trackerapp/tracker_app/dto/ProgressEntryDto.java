package com.trackerapp.tracker_app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgressEntryDto {

    @NotNull(message = "Progress value is required")
    @Positive(message = "Progress value must be greater than zero")
    private Double value;

    @NotNull(message = "Entry date is required")
    @jakarta.validation.constraints.PastOrPresent(message = "Progress date cannot be in the future")
    private LocalDate entryDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}