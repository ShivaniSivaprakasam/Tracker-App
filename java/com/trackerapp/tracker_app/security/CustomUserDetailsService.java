package com.trackerapp.tracker_app.security;

import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges our custom User entity with Spring Security's authentication
 * mechanism. Spring Security calls loadUserByUsername() during every login
 * attempt; we use the user's email as the "username" since that's how
 * they log in per the project spec.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // direct repository access is standard practice for this class

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Look up the user by email; if not found, Spring Security treats this as a failed login attempt
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));


        // Convert our Role entities into Spring Security's GrantedAuthority objects.
// The explicit <GrantedAuthority> type witness on map() is required here —
// without it, Java infers List<SimpleGrantedAuthority>, which is not
// assignable to List<GrantedAuthority> due to generic invariance.
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(role.getName().name())) // e.g. "ROLE_USER"
                .toList();

        // Build Spring Security's internal User object (different from our own User entity)
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())       // already-hashed password from the DB
                .authorities(authorities)
                .disabled(!user.isEnabled() || !user.isEmailVerified()) // blocked if admin-disabled OR email not yet verified
                .disabled(!user.isEnabled())// respects the admin's ability to disable an account (Step 11)
                .build();
    }
}