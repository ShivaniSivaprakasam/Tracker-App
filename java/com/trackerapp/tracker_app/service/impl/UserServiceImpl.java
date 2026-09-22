package com.trackerapp.tracker_app.service.impl;

import com.trackerapp.tracker_app.dto.PasswordChangeDto;
import com.trackerapp.tracker_app.dto.ProfileUpdateDto;
import com.trackerapp.tracker_app.dto.RegisterDto;
import com.trackerapp.tracker_app.entity.*;
import com.trackerapp.tracker_app.exception.DuplicateEmailException;
import com.trackerapp.tracker_app.repository.*;
import com.trackerapp.tracker_app.service.EmailService;
import com.trackerapp.tracker_app.service.UserService;
import com.trackerapp.tracker_app.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogRepository activityLogRepository;
    private final BadgeRepository badgeRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public User registerUser(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }

        PasswordValidator.validate(dto.getPassword(), dto.getEmail(), dto.getName());

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found — check DataSeeder configuration"));

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true);
        user.setEmailVerified(false);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        logActivity(savedUser, ActivityType.REGISTRATION, "Account registered for " + savedUser.getEmail());
        sendVerificationEmail(savedUser);

        return savedUser;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void toggleUserEnabled(Long userId, Long actingAdminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        assertManageable(user, actingAdminId);

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        logActivity(user, ActivityType.PROFILE_UPDATED,
                user.isEnabled() ? "Account re-enabled by admin" : "Account disabled by admin");
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Long actingAdminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        assertManageable(user, actingAdminId);

        // Verification/reset tokens have no cascade relationship configured
        // on User (they're transient auth artifacts, not core domain data),
        // so they must be cleaned up explicitly before the user row can be
        // deleted — otherwise the FK constraint blocks the delete, as seen
        // when this wasn't yet in place.
        verificationTokenRepository.deleteByUserId(userId);
        passwordResetTokenRepository.deleteByUserId(userId);

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void promoteToAdmin(Long userId, Long actingAdminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found — check DataSeeder configuration"));

        user.getRoles().add(adminRole);
        user.setPromotedBy(actingAdminId);
        userRepository.save(user);

        logActivity(user, ActivityType.PROFILE_UPDATED, "Promoted to admin");
    }

    private void assertManageable(User target, Long actingAdminId) {
        if (target.getId().equals(actingAdminId)) {
            throw new AccessDeniedException("You cannot disable or delete your own admin account.");
        }

        if (target.isAdmin()) {
            if (target.getPromotedBy() == null) {
                throw new AccessDeniedException("This is a primary admin account and cannot be modified.");
            }
            if (!target.getPromotedBy().equals(actingAdminId)) {
                throw new AccessDeniedException("Only the admin who promoted this account can manage it.");
            }
        }
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setBio(dto.getBio() != null ? dto.getBio().trim() : null);
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        userRepository.save(user);

        logActivity(user, ActivityType.PROFILE_UPDATED, "Profile information updated");
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }

        PasswordValidator.validate(dto.getNewPassword(), user.getEmail(), user.getName());

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        logActivity(user, ActivityType.PASSWORD_CHANGED, "Password changed");
    }

    @Override
    public int calculateProfileCompletion(User user) {
        int completion = 40;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) completion += 20;
        if (user.getBio() != null && !user.getBio().isBlank()) completion += 20;
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isBlank()) completion += 20;
        return completion;
    }

    @Override
    public List<Badge> getBadgesForUser(Long userId) {
        return badgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }

    @Override
    public List<ActivityLog> getActivityLogForUser(Long userId) {
        return activityLogRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Override
    @Transactional
    public void sendVerificationEmail(User user) {
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusHours(24));
        token.setUser(user);
        verificationTokenRepository.save(token);

        String link = baseUrl + "/verify-email?token=" + token.getToken();
        String body = "Hi " + user.getName() + ",\n\n"
                + "Please verify your email address by clicking the link below:\n" + link
                + "\n\nThis link expires in 24 hours.\n\nIf you didn't create this account, you can ignore this email.";

        try {
            emailService.sendSimpleEmail(user.getEmail(), "Verify your Tracker App account", body);
        } catch (Exception ex) {
            // Transient mail-provider failure shouldn't fail registration itself.
        }
    }

    @Override
    @Transactional
    public boolean verifyEmailToken(String tokenValue) {
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) return false;

        VerificationToken token = tokenOpt.get();
        if (token.isUsed() || token.getExpiryDate().isBefore(LocalDateTime.now())) return false;

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsed(true);
        verificationTokenRepository.save(token);

        return true;
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        if (user.isEmailVerified()) return;

        Optional<VerificationToken> lastToken = verificationTokenRepository.findTopByUserIdOrderByCreatedDateDesc(user.getId());
        if (lastToken.isPresent() && lastToken.get().getCreatedDate().isAfter(LocalDateTime.now().minusSeconds(60))) {
            return;
        }

        sendVerificationEmail(user);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(60));
        token.setUser(user);
        passwordResetTokenRepository.save(token);

        String link = baseUrl + "/reset-password?token=" + token.getToken();
        String body = "Hi " + user.getName() + ",\n\n"
                + "We received a request to reset your password. Click the link below to choose a new one:\n"
                + link + "\n\nThis link expires in 1 hour. If you didn't request this, you can safely ignore this email.";

        try {
            emailService.sendSimpleEmail(user.getEmail(), "Reset your Tracker App password", body);
        } catch (Exception ex) {
            // Same reasoning as sendVerificationEmail.
        }
    }

    @Override
    @Transactional
    public void resetPassword(String tokenValue, String newPassword, String confirmNewPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("This reset link is invalid."));

        if (token.isUsed() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }

        User user = token.getUser();

        PasswordValidator.validate(newPassword, user.getEmail(), user.getName());

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        logActivity(user, ActivityType.PASSWORD_CHANGED, "Password reset via email link");
    }

    private void logActivity(User user, ActivityType type, String description) {
        ActivityLog log = new ActivityLog();
        log.setActionType(type);
        log.setDescription(description);
        log.setUser(user);
        activityLogRepository.save(log);
    }

    @Override
    @Transactional
    public void deactivateOwnAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (user.isAdmin()) {
            throw new AccessDeniedException("Admin accounts cannot be self-deactivated. Contact another admin.");
        }

        user.setEnabled(false);
        userRepository.save(user);

        logActivity(user, ActivityType.PROFILE_UPDATED, "Account deactivated by self");
    }
}