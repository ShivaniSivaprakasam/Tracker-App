package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.service.TrackerService;
import com.trackerapp.tracker_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final TrackerService trackerService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByEmail(userDetails.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("activeTrackers", trackerService.getActiveTrackersForUser(user.getId()));
        model.addAttribute("favoriteTrackers", trackerService.getFavoriteTrackersForUser(user.getId()));
        model.addAttribute("stats", trackerService.getDashboardStats(user.getId()));
        model.addAttribute("completionPercentage", userService.calculateProfileCompletion(user));

        // Side-panel data: recent badges, recent activity, this month's summary.
        List<?> allBadges = userService.getBadgesForUser(user.getId());
        model.addAttribute("recentBadges", allBadges.size() > 5 ? allBadges.subList(0, 5) : allBadges);

        List<?> allActivity = userService.getActivityLogForUser(user.getId());
        model.addAttribute("recentActivity", allActivity.size() > 5 ? allActivity.subList(0, 5) : allActivity);

        LocalDate now = LocalDate.now();
        model.addAttribute("monthlySummary", trackerService.getMonthlySummary(user.getId(), now.getYear(), now.getMonthValue()));

        return "dashboard";
    }
}