package com.trackerapp.tracker_app.service;

import com.trackerapp.tracker_app.dto.PasswordChangeDto;
import com.trackerapp.tracker_app.dto.ProfileUpdateDto;
import com.trackerapp.tracker_app.dto.RegisterDto;
import com.trackerapp.tracker_app.entity.ActivityLog;
import com.trackerapp.tracker_app.entity.Badge;
import com.trackerapp.tracker_app.entity.User;

import java.util.List;

public interface UserService {

    User registerUser(RegisterDto dto);

    List<User> getAllUsers();

    /**
     * @throws org.springframework.security.access.AccessDeniedException if
     *         actingAdminId is not permitted to modify this account (self,
     *         the primary admin, or an admin promoted by someone else).
     */
    void toggleUserEnabled(Long userId, Long actingAdminId);

    void deleteUser(Long userId, Long actingAdminId);

    void promoteToAdmin(Long userId, Long actingAdminId);

    User getUserByEmail(String email);

    void updateProfile(Long userId, ProfileUpdateDto dto);

    void changePassword(Long userId, PasswordChangeDto dto);

    int calculateProfileCompletion(User user);

    List<Badge> getBadgesForUser(Long userId);

    List<ActivityLog> getActivityLogForUser(Long userId);

    void sendVerificationEmail(User user);

    boolean verifyEmailToken(String token);

    void resendVerificationEmail(String email);

    void initiatePasswordReset(String email);

    void resetPassword(String token, String newPassword, String confirmNewPassword);

    void deactivateOwnAccount(Long userId);
}