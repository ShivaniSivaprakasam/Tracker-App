package com.trackerapp.tracker_app.service.impl;

import com.trackerapp.tracker_app.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name:Tracker App}")
    private String senderName;

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        Map<String, Object> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);

        Map<String, Object> recipient = new HashMap<>();
        recipient.put("email", to);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", sender);
        payload.put("to", java.util.List.of(recipient));
        payload.put("subject", subject);
        // textContent sends plain text; Brevo also supports htmlContent if you
        // ever want styled emails instead.
        payload.put("textContent", body);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(BREVO_API_URL, request, String.class);
        } catch (Exception ex) {
            // Surface the failure to the caller (NotificationScheduler already
            // catches and logs/ignores per-recipient failures), but don't let
            // a bad response body crash the app with an unhandled exception type.
            throw new RuntimeException("Failed to send email via Brevo: " + ex.getMessage(), ex);
        }
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
