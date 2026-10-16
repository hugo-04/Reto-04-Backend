package com.reto.customers.mapper;

import com.reto.customers.dto.request.CustomerRequest;
import com.reto.customers.dto.response.CustomerResponse;
import com.reto.customers.entity.Customer;
import com.reto.customers.projection.CustomerView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Locale;

/**
 * Mapper generado por MapStruct en tiempo de compilación.
 * La normalización de datos (espacios y email en minúsculas) se declara aquí
 * para que el resto del código trabaje con valores consistentes.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    // id y createdAt los asigna la base de datos / @PrePersist
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "firstName", qualifiedByName = "trim")
    @Mapping(target = "lastName", qualifiedByName = "trim")
    @Mapping(target = "dni", qualifiedByName = "trim")
    @Mapping(target = "email", qualifiedByName = "normalizeEmail")
    Customer toEntity(CustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    CustomerResponse toResponse(CustomerView view);

    @Named("trim")
    default String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
