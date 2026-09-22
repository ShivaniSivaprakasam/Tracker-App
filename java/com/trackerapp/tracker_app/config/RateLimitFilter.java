package com.trackerapp.tracker_app.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, per-IP rate limiting for sensitive, abuse-prone endpoints
 * (login, forgot-password, resend-verification). Each IP gets a bucket of
 * 5 tokens refilling every minute; once exhausted, further requests get a
 * 429 response instead of reaching the controller.
 *
 * Scope note: this is in-memory and per-instance — correct and sufficient
 * for a single-server deployment. A multi-instance production deployment
 * would need a shared store (e.g. Redis-backed Bucket4j) so limits are
 * enforced consistently across all instances; that's a deliberate,
 * honest scope boundary for this project rather than an oversight.
 */
@Component
public class RateLimitFilter extends HttpFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/login", "/forgot-password", "/resend-verification"
    );

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String path = request.getRequestURI();
        boolean isLimited = LIMITED_PATHS.contains(path) && "POST".equalsIgnoreCase(request.getMethod());

        if (isLimited) {
            String clientIp = resolveClientIp(request);
            Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> newBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429); // 429 Too Many Requests
                response.setContentType("text/plain");
                response.getWriter().write("Too many attempts. Please wait a minute and try again.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    // X-Forwarded-For is checked first since a reverse proxy (e.g. on AWS,
    // Step 27) would otherwise make every request appear to come from the
    // proxy's own IP rather than the real client.
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}