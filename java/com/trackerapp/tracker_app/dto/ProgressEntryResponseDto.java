package com.trackerapp.tracker_app.dto;

import com.trackerapp.tracker_app.entity.ProgressEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProgressEntryResponseDto {

    private Long id;
    private Double value;
    private LocalDate entryDate;
    private String notes;

    public static ProgressEntryResponseDto fromEntity(ProgressEntry entry){
        return new ProgressEntryResponseDto(
                entry.getId(),
                entry.getValue(),
                entry.getEntryDate(),
                entry.getNotes()
        );
    }
}
