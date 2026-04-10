package ru.ssau.todo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Map<String, Object>> response(HttpStatus s, String e, String m, Map<String, Object> a) {
        Map<String, Object> b = new HashMap<>(Map.of("timestamp", LocalDateTime.now(), "status", s.value(), "error", e, "message", m));
        if (a != null) b.putAll(a);
        return ResponseEntity.status(s).body(b);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTaskNotFound(TaskNotFoundException ex, WebRequest request) {
        return response(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(),
                Map.of("path", request.getDescription(false).replace("uri=", "")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return response(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid input data",
                Map.of("details", ex.getBindingResult().getFieldErrors().stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .toList()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return response(HttpStatus.BAD_REQUEST, "Business Rule Violation", ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest r) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                Map.of("path", r.getDescription(false).replace("uri=", "")));
    }
}
