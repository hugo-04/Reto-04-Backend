package com.reto.customers.exception;

/**
 * Se lanza cuando ya existe un cliente con el mismo DNI o email (HTTP 409).
 */
public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String message) {
        super(message);
    }
}
