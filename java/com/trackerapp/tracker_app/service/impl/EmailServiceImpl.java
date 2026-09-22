package com.trackerapp.tracker_app.service.impl;

import com.trackerapp.tracker_app.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendBadgeEarnedEmail(String to, String userName, String badgeName, String badgeDescription) {
        String subject = "You earned a new badge: " + badgeName;
        String body = "Hi " + userName + ",\n\nCongratulations! You just earned the \"" + badgeName + "\" badge.\n"
                + badgeDescription + "\n\nKeep up the great work!\n— Tracker App";
        sendSimpleEmail(to, subject, body);
    }

    @Override
    public void sendGoalCompletedEmail(String to, String userName, String trackerName) {
        String subject = "Goal completed: " + trackerName;
        String body = "Hi " + userName + ",\n\nYou've reached your goal on \"" + trackerName + "\"! Great job staying consistent.\n\n— Tracker App";
        sendSimpleEmail(to, subject, body);
    }

    @Override
    public void sendDeadlineReminderEmail(String to, String userName, String trackerName, int daysRemaining) {
        String subject = daysRemaining + " days left: " + trackerName;
        String body = "Hi " + userName + ",\n\nJust a heads up — your tracker \"" + trackerName + "\" has a deadline in "
                + daysRemaining + " days. Log some progress to stay on track!\n\n— Tracker App";
        sendSimpleEmail(to, subject, body);
    }

    @Override
    public void sendInactivityNudgeEmail(String to, String userName) {
        String subject = "We miss you at Tracker App";
        String body = "Hi " + userName + ",\n\nIt's been a little while since you've logged any progress. Small steps add up —\n"
                + "why not check in on one of your trackers today, or start a new one?\n\n— Tracker App";
        sendSimpleEmail(to, subject, body);
    }
}