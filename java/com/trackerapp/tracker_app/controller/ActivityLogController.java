package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ActivityLogController {

    private final UserService userService;

    @GetMapping("/activity-log")
    public String viewActivityLog(@AuthenticationPrincipal UserDetails userDetails, Model model){

        User user = userService.getUserByEmail(userDetails.getUsername());
        model.addAttribute("logs", userService.getActivityLogForUser(user.getId()));
        return "activity-log";
    }
}
