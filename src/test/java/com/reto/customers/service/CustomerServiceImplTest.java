package com.reto.customers.service;

import com.reto.customers.dto.request.CustomerRequest;
import com.reto.customers.dto.response.CustomerResponse;
import com.reto.customers.entity.Customer;
import com.reto.customers.exception.DuplicateCustomerException;
import com.reto.customers.mapper.CustomerMapper;
import com.reto.customers.repository.CustomerRepository;
import com.reto.customers.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        // Se usa el mapper real generado por MapStruct para probar también la normalización
        service = new CustomerServiceImpl(repository, Mappers.getMapper(CustomerMapper.class));
    }

    @Test
    @DisplayName("create: guarda el cliente normalizando email y espacios")
    void createGuardaClienteNormalizado() {
        CustomerRequest request = new CustomerRequest(
                "  Ana ", "Pérez", "  ANA@Mail.com ", "12345678", LocalDate.of(1990, 5, 20));
        when(repository.existsByDni("12345678")).thenReturn(false);
        when(repository.existsByEmail("ana@mail.com")).thenReturn(false);
        // Simula el INSERT asignando un id al cliente recibido
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            ReflectionTestUtils.setField(customer, "id", 1L);
            return customer;
        });

        CustomerResponse response = service.create(request);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo("Ana");
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@mail.com");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.dni()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("create: lanza DuplicateCustomerException si el DNI ya existe y no guarda")
    void createFallaConDniDuplicado() {
        CustomerRequest request = new CustomerRequest(
                "Ana", "Pérez", "ana@mail.com", "12345678", LocalDate.of(1990, 5, 20));
        when(repository.existsByDni("12345678")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessageContaining("12345678");

        verify(repository, never()).save(any(Customer.class));
    }
}
