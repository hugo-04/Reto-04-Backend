package com.reto.customers.dto.response;

import com.reto.customers.dto.MonthlyBirthStat;

import java.util.List;

/**
 * Respuesta del endpoint de indicadores.
 * {@code highest} y {@code lowest} son null cuando no existen clientes.
 */
public record BirthIndicatorsResponse(
        long totalCustomers,
        List<MonthlyBirthStat> birthsByMonth,
        MonthlyBirthStat highest,
        MonthlyBirthStat lowest
) {
}
