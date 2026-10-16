package com.reto.customers.projection;

/**
 * Proyección del conteo de clientes agrupado por año y mes de nacimiento.
 */
public interface MonthlyBirthCountView {

    Integer getBirthYear();

    Integer getBirthMonth();

    Long getTotal();
}
