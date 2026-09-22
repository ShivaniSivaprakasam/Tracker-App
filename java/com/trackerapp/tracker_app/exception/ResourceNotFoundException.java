package com.trackerapp.tracker_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource (tracker, user, etc.) doesn't exist.
 * The @ResponseStatus annotation tells Spring to automatically set a 404
 * response when this propagates uncaught from a controller — for MVC
 * requests, Spring Boot then automatically renders templates/error/404.html
 * if present, with no extra controller-advice wiring required.
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);
    }
}
