package com.trackerapp.tracker_app.controller;

import com.trackerapp.tracker_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "unverified", required = false) String unverified,
            @RequestParam(value = "disabled", required = false) String disabled,
            @RequestParam(value = "verified", required = false) String verified,
            @RequestParam(value = "verifyError", required = false) String verifyError,
            @RequestParam(value = "resent", required = false) String resent,
            @RequestParam(value = "resetSuccess", required = false) String resetSuccess,
            @RequestParam(value = "deactivated", required = false) String deactivated,
            Model model) {

        if (error != null) model.addAttribute("errorMessage", "Invalid email or password.");
        if (unverified != null) model.addAttribute("errorMessage", "Please verify your email before logging in. Check your inbox, or request a new verification link below.");
        if (disabled != null) model.addAttribute("errorMessage", "This account has been disabled by an administrator.");
        if (logout != null) model.addAttribute("successMessage", "You have been logged out successfully.");
        if (registered != null) model.addAttribute("successMessage", "Registration successful! Please check your email to verify your account before logging in.");
        if (verified != null) model.addAttribute("successMessage", "Email verified successfully! You can now log in.");
        if (verifyError != null) model.addAttribute("errorMessage", "That verification link is invalid or has expired. Please request a new one below.");
        if (resent != null) model.addAttribute("successMessage", "If that email is registered and unverified, a new verification link has been sent.");
        if (resetSuccess != null) model.addAttribute("successMessage", "Password reset successfully. Please log in with your new password.");
        if (deactivated != null) model.addAttribute("successMessage", "Your account has been deactivated. Contact an administrator if you'd like it reactivated.");

        return "login";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        boolean success = userService.verifyEmailToken(token);
        return success ? "redirect:/login?verified" : "redirect:/login?verifyError";
    }

    @GetMapping("/resend-verification")
    public String resendVerificationForm() {
        return "resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email) {
        userService.resendVerificationEmail(email);
        return "redirect:/login?resent";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("email") String email, Model model) {
        userService.initiatePasswordReset(email);
        model.addAttribute("email", email);
        return "forgot-password-sent";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmNewPassword") String confirmNewPassword,
            Model model) {

        try {
            userService.resetPassword(token, newPassword, confirmNewPassword);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }

        return "redirect:/login?resetSuccess";
    }
}