package com.example.inventoryservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Validation failed");
        response.put("errors", fields);
        return response;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleBadRequest(RuntimeException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", ex.getMessage());
        return response;
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleUpstreamError(HttpStatusCodeException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", ex.getResponseBodyAsString() == null || ex.getResponseBodyAsString().isBlank()
                ? "Request failed while calling another service"
                : ex.getResponseBodyAsString());
        response.put("status", ex.getStatusCode().value());
        return response;
    }
}
