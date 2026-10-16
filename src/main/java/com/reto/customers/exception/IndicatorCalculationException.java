package com.reto.customers.exception;

/**
 * Se lanza cuando falla el acceso a datos al calcular los indicadores (HTTP 500).
 */
public class IndicatorCalculationException extends RuntimeException {

    public IndicatorCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
