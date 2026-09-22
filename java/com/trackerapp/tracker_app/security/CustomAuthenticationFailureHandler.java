package com.trackerapp.tracker_app.security;

import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Distinguishes WHY a login attempt failed — wrong credentials, an
 * unverified email, or an admin-disabled account — so the login page can
 * show a specific, actionable message instead of one generic error for
 * every case.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String email = request.getParameter("username");
        String redirectUrl = "/login?error";

        if (exception instanceof DisabledException && email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isEmailVerified()) {
                    redirectUrl = "/login?unverified";
                } else if (!user.isEnabled()) {
                    redirectUrl = "/login?disabled";
                }
            }
        }

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}