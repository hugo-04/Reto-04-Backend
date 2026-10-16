package com.reto.customers.service;

import com.reto.customers.dto.response.BirthIndicatorsResponse;

/**
 * Cálculo de indicadores de natalidad sobre la base de clientes.
 */
public interface IndicatorService {

    /**
     * Calcula: cantidad de nacidos por mes/año, mes/año con mayor y menor cantidad
     * y tasa de natalidad de cada mes.
     * Solo se consideran los meses con al menos un cliente. En caso de empate se devuelve el más antiguo.
     */
    BirthIndicatorsResponse calculate();
}
