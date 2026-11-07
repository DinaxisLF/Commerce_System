package com.dinaxis.ttbackend.exception;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Loop through all the failed validations
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Return the map as a JSON object with a 400 Bad Request status
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessLogicExceptions(HttpClientErrorException ex) {

        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("status", ex.getStatusCode().value());

        errorResponse.put("message", ex.getStatusText());

        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    // Two orders locking the same products in opposite order can deadlock in MySQL; the losing one is rolled back and can be retried.
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleLockConflicts(PessimisticLockingFailureException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("message", "Another order is updating the same products, please retry");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
