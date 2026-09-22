package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/admin/users")
    public String listUsers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentAdmin = userService.getUserByEmail(userDetails.getUsername());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUserId", currentAdmin.getId());
        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/toggle-enabled")
    public String toggleEnabled(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User currentAdmin = userService.getUserByEmail(userDetails.getUsername());
        userService.toggleUserEnabled(id, currentAdmin.getId());
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User currentAdmin = userService.getUserByEmail(userDetails.getUsername());
        userService.deleteUser(id, currentAdmin.getId());
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/promote")
    public String promoteUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id) {
        User currentAdmin = userService.getUserByEmail(userDetails.getUsername());
        userService.promoteToAdmin(id, currentAdmin.getId());
        return "redirect:/admin/users";
    }
}