package com.reto.customers.exception;

import com.reto.customers.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce las excepciones a respuestas HTTP con un formato único (ApiError).
 * Evita repetir try/catch en los controladores.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", details);
    }

    /** JSON mal formado o con un tipo incorrecto (por ejemplo, una fecha que no es yyyy-MM-dd). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es válido. Verifique el JSON y el formato de fecha (yyyy-MM-dd)",
                List.of());
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateCustomerException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(IndicatorCalculationException.class)
    public ResponseEntity<ApiError> handleIndicators(IndicatorCalculationException ex) {
        log.error("Error calculando indicadores", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), List.of());
    }

    /** Último recurso: no se expone el detalle interno al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Error inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), message, details));
    }
}
