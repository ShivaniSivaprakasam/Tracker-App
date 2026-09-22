package com.trackerapp.tracker_app.service;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String body);

    void sendBadgeEarnedEmail(String to, String userName, String badgeName, String badgeDescription);

    void sendGoalCompletedEmail(String to, String userName, String trackerName);

    void sendDeadlineReminderEmail(String to, String userName, String trackerName, int daysRemaining);

    void sendInactivityNudgeEmail(String to, String userName);
}