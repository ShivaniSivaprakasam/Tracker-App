package com.trackerapp.tracker_app.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.trackerapp.tracker_app.dto.ProgressEntryDto;
import com.trackerapp.tracker_app.dto.TrackerCreateDto;
import com.trackerapp.tracker_app.entity.ActivityLog;
import com.trackerapp.tracker_app.entity.ActivityType;
import com.trackerapp.tracker_app.entity.Badge;
import com.trackerapp.tracker_app.entity.Milestone;
import com.trackerapp.tracker_app.entity.ProgressEntry;
import com.trackerapp.tracker_app.entity.Tracker;
import com.trackerapp.tracker_app.entity.TrackerCategory;
import com.trackerapp.tracker_app.entity.TrackerStatus;
import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.repository.ActivityLogRepository;
import com.trackerapp.tracker_app.repository.BadgeRepository;
import com.trackerapp.tracker_app.repository.MilestoneRepository;
import com.trackerapp.tracker_app.repository.ProgressEntryRepository;
import com.trackerapp.tracker_app.repository.TrackerRepository;
import com.trackerapp.tracker_app.repository.UserRepository;
import com.trackerapp.tracker_app.service.EmailService;
import com.trackerapp.tracker_app.service.TrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackerServiceImpl implements TrackerService {

    private final TrackerRepository trackerRepository;
    private final UserRepository userRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final MilestoneRepository milestoneRepository;
    private final BadgeRepository badgeRepository;
    private final ActivityLogRepository activityLogRepository;
    private final EmailService emailService;

    // ==================== Dashboard Data ====================

    @Override
    public List<Tracker> getActiveTrackersForUser(Long userId) {
        return trackerRepository.findByUserIdAndStatus(userId, TrackerStatus.ACTIVE);
    }

    @Override
    public List<Tracker> getFavoriteTrackersForUser(Long userId) {
        return trackerRepository.findByUserIdAndFavoriteTrue(userId);
    }

    private List<Tracker> findNonTrashedTrackers (Long userId){
        return trackerRepository.findByUserId(userId).stream()
                .filter(t -> t.getStatus() != TrackerStatus.TRASHED)
                .collect(Collectors.toList());
    }

    @Override
    public DashboardStats getDashboardStats(Long userId) {
        List<Tracker> allTrackers = findNonTrashedTrackers(userId);

        long total = allTrackers.stream().filter(t -> t.getStatus() != TrackerStatus.TRASHED).count();
        long active = allTrackers.stream().filter(t -> t.getStatus() == TrackerStatus.ACTIVE).count();
        long archived = allTrackers.stream().filter(t -> t.getStatus() == TrackerStatus.ARCHIVED).count();
        long completed = allTrackers.stream().filter(t -> t.getStatus() == TrackerStatus.COMPLETED).count();

        int bestStreak = allTrackers.stream()
                .mapToInt(Tracker::getCurrentStreak)
                .max()
                .orElse(0);

        long badgeCount = badgeRepository.findByUserIdOrderByEarnedAtDesc(userId).size();

        return new DashboardStats(total, active, archived, completed, badgeCount, bestStreak);
    }

    @Override
    public MonthlySummary getMonthlySummary(Long userId, int year, int month) {
        List<Tracker> allTrackers = findNonTrashedTrackers(userId);
        YearMonth targetMonth = YearMonth.of(year, month);

        long completedThisMonth = allTrackers.stream()
                .filter(t -> t.getStatus() == TrackerStatus.COMPLETED)
                .filter(t -> YearMonth.from(t.getUpdatedDate()).equals(targetMonth))
                .count();

        long missedThisMonth = allTrackers.stream()
                .filter(t -> t.getDeadline() != null)
                .filter(t -> YearMonth.from(t.getDeadline()).equals(targetMonth))
                .filter(t -> t.getStatus() != TrackerStatus.COMPLETED)
                .filter(t -> t.getDeadline().isBefore(LocalDate.now()))
                .count();

        String bestTrackerName = allTrackers.stream()
                .max(Comparator.comparingDouble(Tracker::getCompletionPercentage))
                .map(Tracker::getName)
                .orElse("No trackers yet");

        int longestStreak = allTrackers.stream()
                .mapToInt(Tracker::getLongestStreak)
                .max()
                .orElse(0);

        return new MonthlySummary(completedThisMonth, missedThisMonth, bestTrackerName, longestStreak);
    }

    // ==================== Creation / Editing ====================

    @Override
    @Transactional
    public Tracker createTracker(Long userId, TrackerCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Tracker tracker = new Tracker();
        tracker.setName(dto.getName());
        tracker.setCategory(dto.getCategory());
        tracker.setProgressLabel(dto.getProgressLabel());
        tracker.setPurpose(dto.getPurpose());
        tracker.setGoalTarget(dto.getGoalTarget());
        tracker.setDeadline(dto.getDeadline());
        tracker.setCurrentProgress(0.0);
        tracker.setCompletionPercentage(0.0);
        tracker.setStatus(TrackerStatus.ACTIVE);
        tracker.setFavorite(false);
        tracker.setCurrentStreak(0);
        tracker.setLongestStreak(0);
        tracker.setUser(user);

        Tracker saved = trackerRepository.save(tracker);

        logActivity(user, ActivityType.TRACKER_CREATED, "Created tracker \"" + saved.getName() + "\"");

        return saved;
    }

    @Override
    public Tracker getTrackerById(Long trackerId) {
        return trackerRepository.findById(trackerId)
                .orElseThrow(() -> new com.trackerapp.tracker_app.exception.ResourceNotFoundException("Tracker not found with id: " + trackerId));
    }

    @Override
    @Transactional
    public void updateTracker(Long trackerId, TrackerCreateDto dto) {
        Tracker tracker = getTrackerById(trackerId);

        // If the deadline actually changed, allow a new reminder to fire
        // for the new date rather than staying permanently suppressed.
        boolean deadlineChanged = (tracker.getDeadline() == null && dto.getDeadline() != null)
                || (tracker.getDeadline() != null && !tracker.getDeadline().equals(dto.getDeadline()));
        if (deadlineChanged) {
            tracker.setDeadlineReminderSent(false);
        }

        tracker.setName(dto.getName());
        tracker.setProgressLabel(dto.getProgressLabel());
        tracker.setPurpose(dto.getPurpose());
        tracker.setGoalTarget(dto.getGoalTarget());
        tracker.setDeadline(dto.getDeadline());

        trackerRepository.save(tracker);

        logActivity(tracker.getUser(), ActivityType.TRACKER_UPDATED, "Updated tracker \"" + tracker.getName() + "\"");
    }

    // ==================== Progress / Streaks / Milestones ====================

    @Override
    @Transactional
    public void logProgress(Long trackerId, ProgressEntryDto dto) {
        Tracker tracker = getTrackerById(trackerId);

        ProgressEntry entry = new ProgressEntry();
        entry.setValue(dto.getValue());
        entry.setEntryDate(dto.getEntryDate());
        entry.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);
        entry.setTracker(tracker);
        progressEntryRepository.save(entry);

        double newProgress = tracker.getCurrentProgress() + dto.getValue();
        tracker.setCurrentProgress(newProgress);

        double percentage = (newProgress / tracker.getGoalTarget()) * 100;
        tracker.setCompletionPercentage(Math.min(percentage, 100.0));

        boolean justCompleted = false;
        if (newProgress >= tracker.getGoalTarget() && tracker.getStatus() == TrackerStatus.ACTIVE) {
            tracker.setStatus(TrackerStatus.COMPLETED);
            justCompleted = true;
        }

        // ==================== Streak Calculation ====================
        LocalDate previousDate = tracker.getLastProgressDate();
        LocalDate newDate = dto.getEntryDate();

        if (previousDate == null) {
            tracker.setCurrentStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(previousDate, newDate);

            if (daysBetween == 1) {
                tracker.setCurrentStreak(tracker.getCurrentStreak() + 1);
            } else if (daysBetween == 0) {
                // Same-day entry — streak neither grows nor resets.
            } else if (daysBetween > 1) {
                tracker.setCurrentStreak(1);
            }
        }

        if (tracker.getCurrentStreak() > tracker.getLongestStreak()) {
            tracker.setLongestStreak(tracker.getCurrentStreak());
        }

        if (previousDate == null || newDate.isAfter(previousDate)) {
            tracker.setLastProgressDate(newDate);
        }

        checkAndAwardMilestones(tracker);

        trackerRepository.save(tracker);

        if (justCompleted) {
            logActivity(tracker.getUser(), ActivityType.GOAL_COMPLETED, "Completed goal for tracker \"" + tracker.getName() + "\"");
            try {
                emailService.sendGoalCompletedEmail(tracker.getUser().getEmail(), tracker.getUser().getName(), tracker.getName());
            } catch (Exception ex) {
                // Same reasoning as above.
            }
        }
    }

    @Override
    public List<ProgressEntry> getProgressHistory(Long trackerId) {
        return progressEntryRepository.findByTrackerIdOrderByEntryDateDesc(trackerId);
    }

    @Override
    public List<Milestone> getMilestonesForTracker(Long trackerId) {
        return milestoneRepository.findByTrackerIdOrderByAchievedAtDesc(trackerId);
    }

    private void checkAndAwardMilestones(Tracker tracker) {
        List<Milestone> existing = milestoneRepository.findByTrackerIdOrderByAchievedAtDesc(tracker.getId());
        Set<String> alreadyAwardedTitles = existing.stream()
                .map(Milestone::getTitle)
                .collect(Collectors.toSet());

        checkCompletionMilestone(tracker, 25.0, "25% Complete", alreadyAwardedTitles);
        checkCompletionMilestone(tracker, 50.0, "Halfway There", alreadyAwardedTitles);
        checkCompletionMilestone(tracker, 75.0, "75% Complete", alreadyAwardedTitles);
        checkCompletionMilestone(tracker, 100.0, "Goal Achieved", alreadyAwardedTitles);

        checkStreakMilestone(tracker, 7, "7-Day Streak", alreadyAwardedTitles);
        checkStreakMilestone(tracker, 30, "30-Day Streak", alreadyAwardedTitles);
    }

    private void checkCompletionMilestone(Tracker tracker, double threshold, String title, Set<String> alreadyAwarded) {
        if (tracker.getCompletionPercentage() >= threshold && !alreadyAwarded.contains(title)) {
            awardMilestoneAndBadge(tracker, title, "Reached " + (int) threshold + "% completion on \"" + tracker.getName() + "\"");
        }
    }

    private void checkStreakMilestone(Tracker tracker, int days, String title, Set<String> alreadyAwarded) {
        if (tracker.getCurrentStreak() >= days && !alreadyAwarded.contains(title)) {
            awardMilestoneAndBadge(tracker, title, "Maintained a " + days + "-day streak on \"" + tracker.getName() + "\"");
        }
    }

    private void awardMilestoneAndBadge(Tracker tracker, String title, String description) {
        Milestone milestone = new Milestone();
        milestone.setTitle(title);
        milestone.setDescription(description);
        milestone.setAchievedAt(LocalDateTime.now());
        milestone.setTracker(tracker);
        milestoneRepository.save(milestone);

        Badge badge = new Badge();
        badge.setName(title);
        badge.setDescription(description);
        badge.setEarnedAt(LocalDateTime.now());
        badge.setUser(tracker.getUser());
        badgeRepository.save(badge);

        try {
            emailService.sendBadgeEarnedEmail(
                    tracker.getUser().getEmail(),
                    tracker.getUser().getName(),
                    title,
                    description
            );
        } catch (Exception ex) {
            // A transient mail failure must never break milestone/badge
            // logic — the achievement itself is already persisted above.
        }
    }

    // ==================== Favorites / Duplicate / Archive / Restore / Delete ====================

    @Override
    @Transactional
    public void toggleFavorite(Long trackerId) {
        Tracker tracker = getTrackerById(trackerId);
        tracker.setFavorite(!tracker.isFavorite());
        trackerRepository.save(tracker);
    }

    @Override
    @Transactional
    public Tracker duplicateTracker(Long trackerId) {
        Tracker original = getTrackerById(trackerId);

        Tracker copy = new Tracker();
        copy.setName(original.getName() + " (Copy)");
        copy.setCategory(original.getCategory());
        copy.setProgressLabel(original.getProgressLabel());
        copy.setPurpose(original.getPurpose());
        copy.setGoalTarget(original.getGoalTarget());
        copy.setDeadline(original.getDeadline());
        copy.setUser(original.getUser());
        copy.setCurrentProgress(0.0);
        copy.setCompletionPercentage(0.0);
        copy.setStatus(TrackerStatus.ACTIVE);
        copy.setFavorite(false);
        copy.setCurrentStreak(0);
        copy.setLongestStreak(0);

        return trackerRepository.save(copy);
    }

    @Override
    @Transactional
    public void archiveTracker(Long trackerId) {
        Tracker tracker = getTrackerById(trackerId);
        tracker.setStatus(TrackerStatus.ARCHIVED);
        trackerRepository.save(tracker);

        logActivity(tracker.getUser(), ActivityType.TRACKER_ARCHIVED, "Archived tracker \"" + tracker.getName() + "\"");
    }

    @Override
    @Transactional
    public void restoreTracker(Long trackerId) {
        Tracker tracker = getTrackerById(trackerId);
        tracker.setStatus(TrackerStatus.ACTIVE);
        trackerRepository.save(tracker);

        logActivity(tracker.getUser(), ActivityType.TRACKER_RESTORED, "Restored tracker \"" + tracker.getName() + "\"");
    }

    @Override
    @Transactional
    public void deleteTracker(Long trackerId) {
        Tracker tracker = getTrackerById(trackerId);
        trackerRepository.delete(tracker);
    }

    @Override
    public List<Tracker> getArchivedTrackersForUser(Long userId) {
        return trackerRepository.findByUserIdAndStatus(userId, TrackerStatus.ARCHIVED);
    }

    // ==================== Search / Sort / Filter ====================

    @Override
    public List<Tracker> searchSortFilterTrackers(Long userId, String search, TrackerCategory category, TrackerStatus status, String sortBy) {
        List<Tracker> trackers = findNonTrashedTrackers(userId);

        if (search != null && !search.isBlank()) {
            String trimmed = search.trim();
            String lower = trimmed.length() > 100 ? trimmed.substring(0, 100).toLowerCase() : trimmed.toLowerCase(); // simple length cap
            trackers = trackers.stream()
                    .filter(t -> t.getName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }
        if (category != null) {
            trackers = trackers.stream()
                    .filter(t -> t.getCategory() == category)
                    .collect(Collectors.toList());
        }

        if (status != null) {
            trackers = trackers.stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        }

        Comparator<Tracker> comparator = switch (sortBy == null ? "" : sortBy) {
            case "name" -> Comparator.comparing(Tracker::getName, String.CASE_INSENSITIVE_ORDER);
            case "oldest" -> Comparator.comparing(Tracker::getCreatedDate);
            case "goal" -> Comparator.comparingDouble(Tracker::getGoalTarget);
            default -> Comparator.comparing(Tracker::getUpdatedDate).reversed();
        };

        return trackers.stream().sorted(comparator).collect(Collectors.toList());
    }

    // ==================== Export ====================

    @Override
    public String buildCsvExport(Long trackerId) {
        Tracker tracker = getTrackerById(trackerId);
        List<ProgressEntry> history = getProgressHistory(trackerId);

        StringBuilder csv = new StringBuilder();
        csv.append("Tracker Name,Category,Goal Target,Current Progress,Completion %,Status\n");
        csv.append(escapeCsv(tracker.getName())).append(",")
                .append(tracker.getCategory()).append(",")
                .append(tracker.getGoalTarget()).append(",")
                .append(tracker.getCurrentProgress()).append(",")
                .append(tracker.getCompletionPercentage()).append(",")
                .append(tracker.getStatus()).append("\n\n");

        csv.append("Date,Value,Notes\n");
        for (ProgressEntry entry : history) {
            csv.append(entry.getEntryDate()).append(",")
                    .append(entry.getValue()).append(",")
                    .append(escapeCsv(entry.getNotes() != null ? entry.getNotes() : "")).append("\n");
        }

        return csv.toString();
    }

    // Wraps a CSV field in quotes and doubles internal quotes, so commas
    // or quote characters inside a name/note don't corrupt column structure.
    private String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @Override
    public byte[] buildPdfExport(Long trackerId) {
        Tracker tracker = getTrackerById(trackerId);
        List<ProgressEntry> history = getProgressHistory(trackerId);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(tracker.getName()));
            document.add(new Paragraph("Category: " + tracker.getCategory()));
            document.add(new Paragraph("Goal: " + tracker.getCurrentProgress() + " / " + tracker.getGoalTarget()
                    + " (" + tracker.getCompletionPercentage() + "%)"));
            document.add(new Paragraph("Status: " + tracker.getStatus()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Progress History:"));
            for (ProgressEntry entry : history) {
                String line = entry.getEntryDate() + " - " + entry.getValue()
                        + (entry.getNotes() != null && !entry.getNotes().isBlank() ? " (" + entry.getNotes() + ")" : "");
                document.add(new Paragraph(line));
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }

    // ==================== Activity Logging Helper ====================

    private void logActivity(User user, ActivityType type, String description) {
        ActivityLog log = new ActivityLog();
        log.setActionType(type);
        log.setDescription(description);
        log.setUser(user);
        activityLogRepository.save(log);
    }

    @Override
    public Tracker getTrackerForUser(Long trackerId, Long userId) {
        Tracker tracker = getTrackerById(trackerId);
        if (!tracker.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to access this tracker.");
        }
        return tracker;
    }

    @Override
    @Transactional
    public void trashTracker(Long trackerId) {
        Tracker tracker = getTrackerById(trackerId);
        tracker.setStatus(TrackerStatus.TRASHED);
        trackerRepository.save(tracker);

        logActivity(tracker.getUser(), ActivityType.TRACKER_ARCHIVED, "Moved tracker \"" + tracker.getName() + "\" to trash");
    }

    @Override
    public List<Tracker> getTrashedTrackersForUser(Long userId) {
        return trackerRepository.findByUserIdAndStatus(userId, TrackerStatus.TRASHED);
    }

    @Override
    public org.springframework.data.domain.Page<Tracker> getActiveTrackersPaged(Long userId, org.springframework.data.domain.Pageable pageable) {
        return trackerRepository.findByUserIdAndStatus(userId, TrackerStatus.ACTIVE, pageable);
    }

    @Override
    public List<CalendarEvent> getCalendarEventsForMonth(Long userId, int year, int month) {
        YearMonth targetMonth = YearMonth.of(year, month);
        List<Tracker> allTrackers = trackerRepository.findByUserId(userId);
        List<CalendarEvent> events = new java.util.ArrayList<>();

        for (Tracker tracker : allTrackers) {
            for (Milestone m : milestoneRepository.findByTrackerIdOrderByAchievedAtDesc(tracker.getId())) {
                LocalDate achievedDate = m.getAchievedAt().toLocalDate();
                if (YearMonth.from(achievedDate).equals(targetMonth)) {
                    events.add(new CalendarEvent(achievedDate, tracker.getName() + ": " + m.getTitle(), tracker.getId(), "milestone"));
                }
            }

            if (tracker.getStatus() == TrackerStatus.COMPLETED && tracker.getUpdatedDate() != null
                    && YearMonth.from(tracker.getUpdatedDate()).equals(targetMonth)) {
                events.add(new CalendarEvent(tracker.getUpdatedDate().toLocalDate(), tracker.getName() + ": Goal completed", tracker.getId(), "completed"));
            }

            if (tracker.getDeadline() != null && YearMonth.from(tracker.getDeadline()).equals(targetMonth)
                    && tracker.getStatus() != TrackerStatus.COMPLETED && tracker.getDeadline().isBefore(LocalDate.now())) {
                events.add(new CalendarEvent(tracker.getDeadline(), tracker.getName() + ": Deadline missed", tracker.getId(), "missed"));
            }
        }

        return events;
    }
}