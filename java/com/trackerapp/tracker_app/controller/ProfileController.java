package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.dto.PasswordChangeDto;
import com.trackerapp.tracker_app.dto.ProfileUpdateDto;
import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByEmail(userDetails.getUsername());

        ProfileUpdateDto profileDto = new ProfileUpdateDto();
        profileDto.setName(user.getName());
        profileDto.setPhoneNumber(user.getPhoneNumber());
        profileDto.setBio(user.getBio());
        profileDto.setProfilePictureUrl(user.getProfilePictureUrl());

        model.addAttribute("user", user);
        model.addAttribute("profileDto", profileDto);
        model.addAttribute("passwordDto", new PasswordChangeDto());
        model.addAttribute("completionPercentage", userService.calculateProfileCompletion(user));
        model.addAttribute("badges", userService.getBadgesForUser(user.getId()));

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("profileDto") ProfileUpdateDto profileDto,
            BindingResult bindingResult,
            Model model) {

        User user = userService.getUserByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("passwordDto", new PasswordChangeDto());
            model.addAttribute("completionPercentage", userService.calculateProfileCompletion(user));
            model.addAttribute("badges", userService.getBadgesForUser(user.getId()));
            return "profile";
        }

        userService.updateProfile(user.getId(), profileDto);
        return "redirect:/profile?updated";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
            BindingResult bindingResult,
            Model model) {

        User user = userService.getUserByEmail(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profileDto", new ProfileUpdateDto());
            model.addAttribute("completionPercentage", userService.calculateProfileCompletion(user));
            model.addAttribute("badges", userService.getBadgesForUser(user.getId()));
            return "profile";
        }

        try {
            userService.changePassword(user.getId(), passwordDto);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("currentPassword", "error.password", ex.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("profileDto", new ProfileUpdateDto());
            model.addAttribute("completionPercentage", userService.calculateProfileCompletion(user));
            model.addAttribute("badges", userService.getBadgesForUser(user.getId()));
            return "profile";
        }

        return "redirect:/profile?passwordChanged";
    }

    @PostMapping("/profile/deactivate")
    public String deactivateAccount(@AuthenticationPrincipal UserDetails userDetails,
                                    jakarta.servlet.http.HttpServletRequest request) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        userService.deactivateOwnAccount(user.getId());

        request.getSession().invalidate();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        return "redirect:/login?deactivated";
    }
}