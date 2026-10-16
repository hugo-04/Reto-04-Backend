package com.reto.customers.dto;

import java.math.BigDecimal;

/**
 * Indicador de un mes/año.
 *
 * @param year      año de nacimiento
 * @param month     mes de nacimiento (1-12)
 * @param count     cantidad de clientes nacidos en ese mes/año
 * @param birthRate tasa de natalidad: porcentaje que representa {@code count} sobre el total de clientes
 */
public record MonthlyBirthStat(int year, int month, long count, BigDecimal birthRate) {
}
