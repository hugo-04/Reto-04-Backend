package com.reto.customers.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Proyección de interfaz para el listado de clientes.
 * Hibernate solo lee estas columnas y no hidrata la entidad completa.
 */
public interface CustomerView {

    Long getId();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getDni();

    LocalDate getBirthDate();

    LocalDateTime getCreatedAt();
}
