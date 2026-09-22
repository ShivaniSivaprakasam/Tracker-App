package com.trackerapp.tracker_app.exception;

/**
 * Thrown when a registration attempt uses an email address that is
 * already associated with an existing account. Kept as a distinct
 * exception type (rather than a generic RuntimeException) so the
 * controller layer can catch it specifically and show a friendly
 * form error instead of a generic 500 page.
 */


public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(String message){
        super(message);
    }
}
