package com.bellagnech.dig_bank.security.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when JWT token validation fails
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidJwtAuthenticationException extends AuthenticationException {
    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }
    
    public InvalidJwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
