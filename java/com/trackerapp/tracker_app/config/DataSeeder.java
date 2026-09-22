package com.trackerapp.tracker_app.config;

import com.trackerapp.tracker_app.entity.Role;
import com.trackerapp.tracker_app.entity.RoleName;
import com.trackerapp.tracker_app.entity.User;
import com.trackerapp.tracker_app.repository.RoleRepository;
import com.trackerapp.tracker_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs once at application startup. Ensures the two core roles (ROLE_USER,
 * ROLE_ADMIN) exist, and seeds a single default admin account so there is
 * always at least one admin able to log in and promote other users.
 * Idempotent — safe to run on every startup since it checks for existence
 * before inserting anything.
 */

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // Default admin credentials — intended to be changed immediately after first login.
    // In a real production deployment this would come from an environment
    // variable or secrets manager, not a hardcoded value; flagged here for
    // when we reach the AWS deployment step.
    private static final String DEFAULT_ADMIN_EMAIL = "shivaniavanikkarasi@gmail.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    @Override
    public void run(String... args) {
        seedRoleIfMissing(RoleName.ROLE_USER);  // standard role assigned to every new registrant
        seedRoleIfMissing(RoleName.ROLE_ADMIN);// elevated role, manually assigned later (Step 11)
        seedDefaultAdminIfMissing();
    }

    private void seedRoleIfMissing(RoleName roleName) {
        // Only insert if this role doesn't already exist — prevents duplicate rows on every restart
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }

    private void seedDefaultAdminIfMissing(){
        // Skip entirely if an admin account already exists — prevents
        // duplicate inserts and accidental password resets on every restart.
        if(userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)){
            return;
        }
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow( () -> new IllegalStateException("ROLE_ADMIN not found - role seeding must run first"));
        User admin = new User();
        admin.setName("Default Admin");
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD)); // hashed, never stored in plain text
        admin.setEnabled(true);
        admin.setEmailVerified(true); // seeded admin skips the email-verification flow

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);

        userRepository.save(admin);
    }
}