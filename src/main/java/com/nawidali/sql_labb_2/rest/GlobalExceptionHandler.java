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
 * Maps domain exceptions to HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Missing required parameter");
        response.put("message", "Parameter '" + ex.getParameterName() + "' is required");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Invalid input");
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SelectException.class)
    public ResponseEntity<Map<String, Object>> handleSelectException(SelectException ex) {
        log.error("Database select error: {}", ex.getMessage(), ex);
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("error", "Database query error");
        response.put("message", "Failed to retrieve data");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InsertException.class)
    public ResponseEntity<Map<String, Object>> handleInsertException(InsertException ex) {
        log.error("Database insert error: {}", ex.getMessage(), ex);
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("error", "Data modification failed");
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConnectionException.class)
    public ResponseEntity<Map<String, Object>> handleConnectionException(ConnectionException ex) {
        log.error("Database connection error: {}", ex.getMessage(), ex);
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("error", "Database connection error");
        response.put("message", "Service temporarily unavailable");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

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
