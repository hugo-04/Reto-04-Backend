package com.reto.customers.service;

import com.reto.customers.dto.request.CustomerRequest;
import com.reto.customers.dto.response.CustomerResponse;

import java.util.List;

/**
 * Casos de uso de clientes.
 */
public interface CustomerService {

    /**
     * Crea un cliente.
     *
     * @throws com.reto.customers.exception.DuplicateCustomerException si el DNI o email ya existen
     */
    CustomerResponse create(CustomerRequest request);

    /**
     * Lista clientes. Los filtros son opcionales y se combinan con AND;
     * sin filtros devuelve todos, del más reciente al más antiguo.
     */
    List<CustomerResponse> find(String dni, String email);
}
