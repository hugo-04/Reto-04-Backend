package com.reto.customers.service.impl;

import com.reto.customers.dto.request.CustomerRequest;
import com.reto.customers.dto.response.CustomerResponse;
import com.reto.customers.entity.Customer;
import com.reto.customers.exception.DuplicateCustomerException;
import com.reto.customers.mapper.CustomerMapper;
import com.reto.customers.repository.CustomerRepository;
import com.reto.customers.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        // La entidad ya trae los datos normalizados (trim y email en minúsculas)
        Customer customer = mapper.toEntity(request);

        if (repository.existsByDni(customer.getDni())) {
            throw new DuplicateCustomerException("Ya existe un cliente con el DNI " + customer.getDni());
        }
        if (repository.existsByEmail(customer.getEmail())) {
            throw new DuplicateCustomerException("Ya existe un cliente con el email " + customer.getEmail());
        }

        try {
            Customer saved = repository.save(customer);
            log.info("Cliente creado con id {}", saved.getId());
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // Respaldo ante concurrencia: otra petición pudo insertar el mismo DNI/email
            // después de las validaciones anteriores; la restricción UNIQUE de la BD lo detecta.
            log.warn("Violación de unicidad al guardar cliente con DNI {}", customer.getDni());
            throw new DuplicateCustomerException("El DNI o el email ya están registrados");
        }
    }

    /**
     * Consulta de solo lectura: no abre transacción propia, basta con la del repositorio.
     * Los filtros se normalizan y se delegan a la base de datos (no se filtra en memoria).
     */
    @Override
    public List<CustomerResponse> find(String dni, String email) {
        return repository.findByFilters(normalizeDni(dni), normalizeEmail(email))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Un filtro vacío se convierte en null para que la consulta lo ignore. */
    private String normalizeDni(String dni) {
        return StringUtils.hasText(dni) ? dni.trim() : null;
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase(Locale.ROOT) : null;
    }
}
