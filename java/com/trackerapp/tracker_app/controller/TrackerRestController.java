package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.dto.ApiResponse;
import com.trackerapp.tracker_app.dto.ProgressEntryDto;
import com.trackerapp.tracker_app.dto.ProgressEntryResponseDto;
import com.trackerapp.tracker_app.dto.TrackerCreateDto;
import com.trackerapp.tracker_app.dto.TrackerResponseDto;
import com.trackerapp.tracker_app.entity.Tracker;
import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.service.TrackerService;
import com.trackerapp.tracker_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trackers")
@RequiredArgsConstructor
public class TrackerRestController {

    private final TrackerService trackerService;
    private final UserService userService;

    private User currentUser(UserDetails userDetails) {
        return userService.getUserByEmail(userDetails.getUsername());
    }

    /**
     * Paginated, wrapped listing of the caller's active trackers.
     * Example: GET /api/trackers?page=0&size=10&sort=name,asc
     */
    @GetMapping
    public ApiResponse<Page<TrackerResponseDto>> getAllTrackers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedDate,desc") String sort) {

        User user = currentUser(userDetails);

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        int cappedSize = Math.min(size, 50); // prevents a client from requesting an excessively large page
        Pageable pageable = PageRequest.of(page, cappedSize, Sort.by(direction, sortParts[0]));

        Page<TrackerResponseDto> result = trackerService.getActiveTrackersPaged(user.getId(), pageable)
                .map(TrackerResponseDto::fromEntity);

        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackerResponseDto> getTracker(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User user = currentUser(userDetails);
        Tracker tracker = trackerService.getTrackerForUser(id, user.getId());
        return ResponseEntity.ok(TrackerResponseDto.fromEntity(tracker));
    }

    @PostMapping
    public ResponseEntity<TrackerResponseDto> createTracker(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TrackerCreateDto dto) {

        User user = currentUser(userDetails);
        Tracker created = trackerService.createTracker(user.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(TrackerResponseDto.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackerResponseDto> updateTracker(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id,
            @Valid @RequestBody TrackerCreateDto dto) {

        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.updateTracker(id, dto);
        return ResponseEntity.ok(TrackerResponseDto.fromEntity(trackerService.getTrackerById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTracker(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.deleteTracker(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<TrackerResponseDto> logProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id,
            @Valid @RequestBody ProgressEntryDto dto) {

        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.logProgress(id, dto);
        return ResponseEntity.ok(TrackerResponseDto.fromEntity(trackerService.getTrackerById(id)));
    }

    @GetMapping("/{id}/progress")
    public List<ProgressEntryResponseDto> getProgressHistory(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        return trackerService.getProgressHistory(id).stream()
                .map(ProgressEntryResponseDto::fromEntity)
                .toList();
    }
}