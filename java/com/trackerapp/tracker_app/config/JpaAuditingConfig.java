package com.trackerapp.tracker_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA Auditing across the application.
 * This allows entities extending BaseEntity to automatically populate
 * createdDate and updatedDate fields without manual intervention in service logic.
 */

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
