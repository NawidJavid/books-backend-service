package com.nawidali.sql_labb_2.rest;

import com.nawidali.sql_labb_2.model.exceptions.ConnectionException;
import com.nawidali.sql_labb_2.model.exceptions.InsertException;
import com.nawidali.sql_labb_2.model.exceptions.SelectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * 
 * Design decision: Map domain exceptions to appropriate HTTP status codes.
 * - 400 for validation/input errors
 * - 404 when data not found (handled in controller)
 * - 500 for unexpected/server errors
 * 
 * This centralizes error handling and keeps controllers clean.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations.
     * Returns 400 Bad Request with field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Validation failed");
        response.put("details", fieldErrors);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle missing required request parameters.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Missing required parameter");
        response.put("message", "Parameter '" + ex.getParameterName() + "' is required");
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle IllegalArgumentException (input validation in controller).
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Invalid input");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle SelectException from database layer.
     * Could be data not found or query error - map to 500 (server error)
     * since query failures are typically not client's fault.
     */
    @ExceptionHandler(SelectException.class)
    public ResponseEntity<Map<String, Object>> handleSelectException(SelectException ex) {
        log.error("Database select error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("error", "Database query error");
        response.put("message", "Failed to retrieve data");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle InsertException from database layer.
     * Could be constraint violation (400) or connection issue (500).
     * Default to 400 as most insert failures are due to invalid data.
     */
    @ExceptionHandler(InsertException.class)
    public ResponseEntity<Map<String, Object>> handleInsertException(InsertException ex) {
        log.error("Database insert error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Data modification failed");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle ConnectionException from database layer.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(ConnectionException.class)
    public ResponseEntity<Map<String, Object>> handleConnectionException(ConnectionException ex) {
        log.error("Database connection error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("error", "Database connection error");
        response.put("message", "Service temporarily unavailable");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Catch-all handler for unexpected exceptions.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("error", "Internal server error");
        response.put("message", "An unexpected error occurred");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
