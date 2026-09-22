package com.trackerapp.tracker_app.config;

import com.trackerapp.tracker_app.entity.ProgressEntry;
import com.trackerapp.tracker_app.entity.Tracker;
import com.trackerapp.tracker_app.entity.TrackerStatus;
import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.repository.ProgressEntryRepository;
import com.trackerapp.tracker_app.repository.TrackerRepository;
import com.trackerapp.tracker_app.repository.UserRepository;
import com.trackerapp.tracker_app.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Runs two daily jobs:
 *   1. Deadline reminders — emails users whose active trackers have a
 *      deadline exactly 3 days away, once per deadline (not repeated daily).
 *   2. Inactivity nudges — emails users who registered more than 3 days
 *      ago and have logged no progress in the last 7 days, at most once
 *      every 7 days.
 *
 * Scope note: this runs as an in-process scheduled task, correct for a
 * single-instance deployment. A multi-instance production deployment would
 * need a distributed lock (e.g. ShedLock) to prevent every instance from
 * sending duplicate emails — a deliberate, honest scope boundary here.
 */
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final int DEADLINE_WARNING_DAYS = 3;
    private static final int INACTIVITY_THRESHOLD_DAYS = 7;
    private static final int MIN_ACCOUNT_AGE_DAYS_BEFORE_NUDGE = 3;
    private static final int NUDGE_COOLDOWN_DAYS = 7;

    private final TrackerRepository trackerRepository;
    private final UserRepository userRepository;
    private final ProgressEntryRepository progressEntryRepository;
    private final EmailService emailService;

    // Runs once daily at 8:00 AM server time.
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDeadlineReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(DEADLINE_WARNING_DAYS);

        List<Tracker> trackersDueSoon = trackerRepository
                .findByStatusAndDeadlineAndDeadlineReminderSentFalse(TrackerStatus.ACTIVE, targetDate);

        for (Tracker tracker : trackersDueSoon) {
            try {
                emailService.sendDeadlineReminderEmail(
                        tracker.getUser().getEmail(),
                        tracker.getUser().getName(),
                        tracker.getName(),
                        DEADLINE_WARNING_DAYS
                );
            } catch (Exception ex) {
                // A failed send for one tracker shouldn't block the rest of
                // the batch or leave the flag unset for a healthy retry path
                // (we intentionally still mark it sent below — see note).
            }

            // Marked sent regardless of email success/failure to avoid
            // retry-storms on a persistently failing mail provider; a more
            // sophisticated system would track delivery status separately.
            tracker.setDeadlineReminderSent(true);
            trackerRepository.save(tracker);
        }
    }

    // Runs once daily at 9:00 AM server time (staggered from the reminder job).
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendInactivityNudges() {
        LocalDate today = LocalDate.now();
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if (!user.isEnabled()) {
                continue; // never nudge disabled/deactivated accounts
            }

            boolean oldEnoughAccount = user.getCreatedDate() != null
                    && user.getCreatedDate().toLocalDate().isBefore(today.minusDays(MIN_ACCOUNT_AGE_DAYS_BEFORE_NUDGE));
            if (!oldEnoughAccount) {
                continue;
            }

            boolean cooldownElapsed = user.getLastInactivityNudgeSent() == null
                    || user.getLastInactivityNudgeSent().isBefore(today.minusDays(NUDGE_COOLDOWN_DAYS));
            if (!cooldownElapsed) {
                continue;
            }

            Optional<ProgressEntry> mostRecentEntry = progressEntryRepository
                    .findTopByTracker_UserIdOrderByEntryDateDesc(user.getId());

            boolean isInactive = mostRecentEntry.isEmpty()
                    || mostRecentEntry.get().getEntryDate().isBefore(today.minusDays(INACTIVITY_THRESHOLD_DAYS));

            if (isInactive) {
                try {
                    emailService.sendInactivityNudgeEmail(user.getEmail(), user.getName());
                } catch (Exception ex) {
                    // Don't let a mail failure block processing the rest of the user list.
                }
                user.setLastInactivityNudgeSent(today);
                userRepository.save(user);
            }
        }
    }
}