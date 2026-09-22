package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.dto.RegisterDto;
import com.trackerapp.tracker_app.exception.DuplicateEmailException;
import com.trackerapp.tracker_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registerDto") RegisterDto registerDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registerDto);
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "error.email", ex.getMessage());
            return "register";
        } catch (IllegalArgumentException ex) {
            // Thrown by PasswordValidator for weak/common/username-containing passwords.
            bindingResult.rejectValue("password", "error.password", ex.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
}