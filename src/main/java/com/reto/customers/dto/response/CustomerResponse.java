package com.reto.customers.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representación pública de un cliente (no expone la entidad JPA).
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String dni,
        LocalDate birthDate,
        LocalDateTime createdAt
) {
}
