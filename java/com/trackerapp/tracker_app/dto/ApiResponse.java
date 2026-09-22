package com.trackerapp.tracker_app.dto;

import java.time.LocalDateTime;

/**
 * Generic envelope for REST API responses, applied to list/paginated
 * endpoints so clients get a consistent shape (success flag, message,
 * payload, timestamp) rather than a bare array or object. Existing
 * single-resource CRUD endpoints (GET/POST/PUT/DELETE by id) are left
 * returning their DTOs directly, to avoid a large retrofit across
 * already-working endpoints for modest benefit.
 */


public record ApiResponse<T>  (boolean success, String message, T data, LocalDateTime timestamp) {

    public static <T> ApiResponse<T> ok(T data, String message){
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> ok(T data){
        return ok(data, "OK");
    }
}
