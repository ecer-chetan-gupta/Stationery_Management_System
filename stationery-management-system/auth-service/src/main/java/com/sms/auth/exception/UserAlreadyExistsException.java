package com.sms.auth.exception;

/**
 * Thrown when a user attempts to register with an email that already exists.
 * Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
