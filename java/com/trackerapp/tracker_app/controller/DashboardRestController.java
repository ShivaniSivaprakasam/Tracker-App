package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.service.TrackerService;
import com.trackerapp.tracker_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST endpoints exposing the same dashboard statistics and monthly
 * summary report shown in the web UI, in JSON form (spec point #32).
 */

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardRestController {

    private final TrackerService trackerService;
    private final UserService userService;

    @GetMapping("/dashboard/stats")
    public TrackerService.DashboardStats getDashboardStats(@AuthenticationPrincipal UserDetails userDetails){

        User user = userService.getUserByEmail(userDetails.getUsername());
        return trackerService.getDashboardStats(user.getId());
    }

    @GetMapping("/reports/monthly-summary")
    public TrackerService.MonthlySummary getMonthlySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month
    ){

        User user = userService.getUserByEmail(userDetails.getUsername());
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        return trackerService.getMonthlySummary(user.getId(), targetYear, targetMonth);
    }
}
