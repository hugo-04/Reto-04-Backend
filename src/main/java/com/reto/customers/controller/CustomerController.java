package com.reto.customers.controller;

import com.reto.customers.dto.response.BirthIndicatorsResponse;
import com.reto.customers.dto.request.CustomerRequest;
import com.reto.customers.dto.response.CustomerResponse;
import com.reto.customers.service.CustomerService;
import com.reto.customers.service.IndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST de clientes. Solo recibe y responde HTTP;
 * la lógica vive en los servicios y los errores los maneja GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final IndicatorService indicatorService;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<CustomerResponse> find(@RequestParam(required = false) String dni,
                                       @RequestParam(required = false) String email) {
        return customerService.find(dni, email);
    }

    @GetMapping("/indicators")
    public BirthIndicatorsResponse indicators() {
        return indicatorService.calculate();
    }
}
