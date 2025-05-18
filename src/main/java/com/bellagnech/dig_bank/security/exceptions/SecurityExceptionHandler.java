package com.bellagnech.dig_bank.security.exceptions;

import com.bellagnech.dig_bank.exceptions.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

/**
 * Exception handler for security-related exceptions
 */
@RestControllerAdvice
@Slf4j
public class SecurityExceptionHandler {

    @ExceptionHandler(InvalidJwtAuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleInvalidJwtAuthenticationException(
            InvalidJwtAuthenticationException ex, WebRequest request) {
        log.error("JWT authentication error: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "JWT Authentication Error: " + ex.getMessage(),
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        log.error("User not found: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        log.error("User already exists: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleRoleNotFoundException(
            RoleNotFoundException ex, WebRequest request) {
        log.error("Role not found: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        log.error("Invalid credentials: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "Invalid username or password",
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDetails> handleDisabledException(
            DisabledException ex, WebRequest request) {
        log.error("Account disabled: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "Account is disabled",
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorDetails> handleLockedException(
            LockedException ex, WebRequest request) {
        log.error("Account locked: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "Account is locked",
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        // Log the request details
        if (request instanceof ServletWebRequest) {
            HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();
            log.error("Access denied for path: {}, method: {}, user: {}", 
                httpRequest.getRequestURI(), 
                httpRequest.getMethod(),
                httpRequest.getUserPrincipal() != null ? httpRequest.getUserPrincipal().getName() : "anonymous");
        }
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "Access denied: You don't have permission to access this resource",
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                "Authentication failed: " + ex.getMessage(),
                request.getDescription(false));
                
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
}
