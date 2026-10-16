package com.reto.customers.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato uniforme para todas las respuestas de error de la API.
 */
public record ApiError(LocalDateTime timestamp, int status, String error, String message, List<String> details) {

    public static ApiError of(int status, String error, String message, List<String> details) {
        return new ApiError(LocalDateTime.now(), status, error, message, details);
    }
}
