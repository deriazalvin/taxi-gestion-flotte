package com.example.taxi.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> violations
) {

    public record FieldViolation(String field, String message) {
    }
}