package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.dto.ProgressEntryDto;
import com.trackerapp.tracker_app.dto.TrackerCreateDto;
import com.trackerapp.tracker_app.entity.Tracker;
import com.trackerapp.tracker_app.entity.TrackerCategory;
import com.trackerapp.tracker_app.entity.TrackerStatus;
import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.service.TrackerService;
import com.trackerapp.tracker_app.service.TrackerTemplate;
import com.trackerapp.tracker_app.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TrackerController {

    private final TrackerService trackerService;
    private final UserService userService;

    private User currentUser(UserDetails userDetails) {
        return userService.getUserByEmail(userDetails.getUsername());
    }

    // ==================== Creation ====================

    @GetMapping("/trackers/new")
    public String newTrackerChoice() {
        return "tracker-new-choice";
    }

    @GetMapping("/trackers/new/predefined")
    public String choosePredefinedCategory(Model model) {
        model.addAttribute("categories", TrackerCategory.values());
        return "tracker-predefined-choice";
    }

    @GetMapping("/trackers/new/predefined/{category}")
    public String predefinedForm(@PathVariable("category") TrackerCategory category, Model model) {
        model.addAttribute("trackerDto", TrackerTemplate.buildDefault(category));
        model.addAttribute("isPredefined", true);
        return "tracker-form";
    }

    @GetMapping("/trackers/new/custom")
    public String customForm(Model model) {
        TrackerCreateDto dto = new TrackerCreateDto();
        dto.setCategory(TrackerCategory.CUSTOM);
        model.addAttribute("trackerDto", dto);
        model.addAttribute("isPredefined", false);
        return "tracker-form";
    }

    @PostMapping("/trackers/new")
    public String createTracker(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("trackerDto") TrackerCreateDto trackerDto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isPredefined", false);
            return "tracker-form";
        }

        User user = currentUser(userDetails);
        trackerService.createTracker(user.getId(), trackerDto);

        return "redirect:/dashboard?trackerCreated";
    }

    // ==================== Editing ====================

    @GetMapping("/trackers/{id}/edit")
    public String editTrackerForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id, Model model) {
        User user = currentUser(userDetails);
        Tracker tracker = trackerService.getTrackerForUser(id, user.getId());

        TrackerCreateDto dto = new TrackerCreateDto();
        dto.setName(tracker.getName());
        dto.setCategory(tracker.getCategory());
        dto.setProgressLabel(tracker.getProgressLabel());
        dto.setPurpose(tracker.getPurpose());
        dto.setGoalTarget(tracker.getGoalTarget());
        dto.setDeadline(tracker.getDeadline());

        model.addAttribute("trackerDto", dto);
        model.addAttribute("isPredefined", false);
        model.addAttribute("editingTrackerId", id);
        return "tracker-form";
    }

    @PostMapping("/trackers/{id}/edit")
    public String updateTracker(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("trackerDto") TrackerCreateDto trackerDto,
            BindingResult bindingResult,
            Model model) {

        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId()); // ownership check — throws before any write if not the owner

        if (bindingResult.hasErrors()) {
            model.addAttribute("isPredefined", false);
            model.addAttribute("editingTrackerId", id);
            return "tracker-form";
        }

        trackerService.updateTracker(id, trackerDto);
        return "redirect:/dashboard?trackerUpdated";
    }

    // ==================== Progress ====================

    @GetMapping("/trackers/{id}/progress")
    public String logProgressForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id, Model model) {
        User user = currentUser(userDetails);
        Tracker tracker = trackerService.getTrackerForUser(id, user.getId());

        ProgressEntryDto dto = new ProgressEntryDto();
        dto.setEntryDate(LocalDate.now());

        model.addAttribute("tracker", tracker);
        model.addAttribute("progressDto", dto);
        model.addAttribute("history", trackerService.getProgressHistory(id));
        model.addAttribute("milestones", trackerService.getMilestonesForTracker(id));
        return "tracker-progress";
    }

    @PostMapping("/trackers/{id}/progress")
    public String submitProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("progressDto") ProgressEntryDto progressDto,
            BindingResult bindingResult,
            Model model) {

        User user = currentUser(userDetails);
        Tracker tracker = trackerService.getTrackerForUser(id, user.getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("tracker", tracker);
            model.addAttribute("history", trackerService.getProgressHistory(id));
            model.addAttribute("milestones", trackerService.getMilestonesForTracker(id));
            return "tracker-progress";
        }

        trackerService.logProgress(id, progressDto);
        return "redirect:/trackers/" + id + "/progress?logged";
    }

    // ==================== Favorites / Duplicate / Archive / Restore / Delete ====================

    @PostMapping("/trackers/{id}/favorite")
    public String toggleFavorite(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.toggleFavorite(id);
        return "redirect:/dashboard";
    }

    @PostMapping("/trackers/{id}/duplicate")
    public String duplicateTracker(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.duplicateTracker(id);
        return "redirect:/dashboard?trackerDuplicated";
    }

    @PostMapping("/trackers/{id}/archive")
    public String archiveTracker(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.archiveTracker(id);
        return "redirect:/dashboard?trackerArchived";
    }

    @PostMapping("/trackers/{id}/restore")
    public String restoreTracker(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id,
                                 jakarta.servlet.http.HttpServletRequest request) {
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.restoreTracker(id);

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/trackers/trash")) {
            return "redirect:/trackers/trash?trackerRestored";
        }
        return "redirect:/trackers/archived?trackerRestored";
    }

    @PostMapping("/trackers/{id}/delete")
    public String deleteTracker(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.deleteTracker(id);
        return "redirect:/trackers/archived?trackerDeleted";
    }

    @GetMapping("/trackers/archived")
    public String archivedTrackers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        model.addAttribute("archivedTrackers", trackerService.getArchivedTrackersForUser(user.getId()));
        return "tracker-archived";
    }

    // ==================== Monthly Summary ====================

    @GetMapping("/trackers/monthly-summary")
    public String monthlySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            Model model) {

        User user = currentUser(userDetails);

        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        model.addAttribute("summary", trackerService.getMonthlySummary(user.getId(), targetYear, targetMonth));
        model.addAttribute("year", targetYear);
        model.addAttribute("month", targetMonth);

        List<TrackerService.CalendarEvent> events = trackerService.getCalendarEventsForMonth(user.getId(), targetYear, targetMonth);

        java.time.YearMonth ym = java.time.YearMonth.of(targetYear, targetMonth);
        int daysInMonth = ym.lengthOfMonth();
        int firstDayOfWeek = ym.atDay(1).getDayOfWeek().getValue() % 7; // Sunday = 0, Saturday = 6

        List<CalendarCell> cells = new java.util.ArrayList<>();
        for (int i = 0; i < firstDayOfWeek; i++) {
            cells.add(new CalendarCell(null, false, null, null, null));
        }
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate cellDate = ym.atDay(day);
            List<TrackerService.CalendarEvent> dayEvents = events.stream()
                    .filter(e -> e.date().equals(cellDate))
                    .toList();

            if (!dayEvents.isEmpty()) {
                TrackerService.CalendarEvent first = dayEvents.get(0);
                String tooltip = dayEvents.stream()
                        .map(TrackerService.CalendarEvent::label)
                        .collect(java.util.stream.Collectors.joining("; "));
                cells.add(new CalendarCell(day, true, first.trackerId(), tooltip, first.type()));
            } else {
                cells.add(new CalendarCell(day, false, null, null, null));
            }
        }
        model.addAttribute("calendarCells", cells);

        return "monthly-summary";
    }

    // ==================== Search / Sort / Filter ====================

    @GetMapping("/trackers")
    public String myTrackers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "q", required = false) String search,
            @RequestParam(value = "category", required = false) TrackerCategory category,
            @RequestParam(value = "status", required = false) TrackerStatus status,
            @RequestParam(value = "sort", required = false) String sort,
            Model model) {

        User user = currentUser(userDetails);
        model.addAttribute("trackers",
                trackerService.searchSortFilterTrackers(user.getId(), search, category, status, sort));
        model.addAttribute("categories", TrackerCategory.values());
        model.addAttribute("statuses", TrackerStatus.values());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSort", sort);

        return "my-trackers";
    }

    // ==================== Export ====================

    @GetMapping("/trackers/{id}/export/csv")
    public void exportCsv(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        User user = currentUser(userDetails);
        Tracker tracker = trackerService.getTrackerForUser(id, user.getId());
        String csv = trackerService.buildCsvExport(id);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + sanitizeFilename(tracker.getName()) + "_history.csv\"");
        response.getWriter().write(csv);
        response.getWriter().flush();
    }

    @GetMapping("/trackers/{id}/export/pdf")
    public void exportPdf(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id, HttpServletResponse response) throws IOException {
        User user = currentUser(userDetails);
        Tracker tracker = trackerService.getTrackerForUser(id, user.getId());
        byte[] pdfBytes = trackerService.buildPdfExport(id);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + sanitizeFilename(tracker.getName()) + "_history.pdf\"");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    @PostMapping("/trackers/{id}/trash")
    public String trashTracker(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id){
        User user = currentUser(userDetails);
        trackerService.getTrackerForUser(id, user.getId());
        trackerService.trashTracker(id);
        return "redirect:/dashboard?trackerTrashed";
    }

    @GetMapping("/trackers/trash")
    public String trashPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        model.addAttribute("trashedTrackers", trackerService.getTrashedTrackersForUser(user.getId()));
        return "tracker-trash";
    }

    public record CalendarCell(Integer day, boolean hasEvent, Long trackerId, String tooltip, String type) {}



}