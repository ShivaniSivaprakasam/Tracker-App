package com.trackerapp.tracker_app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Enables auto-generated OpenAPI/Swagger documentation for every
 * @RestController in the app, available at /swagger-ui.html when running
 * locally. Deliberately not linked anywhere in the app's own navigation —
 * intended to be referenced separately (e.g. from the project's GitHub
 * README) rather than exposed to end users of the live app.
 */

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Tracker App REST API",
                version = "1.0",
                description = "REST endpoints for tracker management, progress logging and dashboard statistics. " + "Authentication uses the same session-based login as the web app (POST /login first)."
        )
)
public class OpenApiConfig {
}
