package com.reto.customers.repository;

import com.reto.customers.entity.Customer;
import com.reto.customers.projection.CustomerView;
import com.reto.customers.repository.projection.MonthlyBirthCountView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Acceso a datos de clientes. Todas las consultas son JPQL explícitas y los filtros
 * se resuelven en la base de datos, de modo que solo viajan las filas necesarias.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("select count(c) > 0 from Customer c where c.dni = :dni")
    boolean existsByDni(@Param("dni") String dni);

    @Query("select count(c) > 0 from Customer c where c.email = :email")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Lista clientes con filtros opcionales: un parámetro null no restringe la consulta.
     * El filtrado ocurre en MySQL (apoyado en los índices únicos de dni y email).
     */
    @Query("""
            select c.id as id,
                   c.firstName as firstName,
                   c.lastName as lastName,
                   c.email as email,
                   c.dni as dni,
                   c.birthDate as birthDate,
                   c.createdAt as createdAt
            from Customer c
            where (:dni is null or c.dni = :dni)
              and (:email is null or c.email = :email)
            order by c.createdAt desc, c.id desc
            """)
    List<CustomerView> findByFilters(@Param("dni") String dni, @Param("email") String email);

    /**
     * Cuenta los clientes por año y mes de nacimiento, en orden cronológico.
     * La agregación se hace en base de datos para no cargar los clientes en memoria.
     */
    @Query("""
            select year(c.birthDate) as birthYear,
                   month(c.birthDate) as birthMonth,
                   count(c) as total
            from Customer c
            group by year(c.birthDate), month(c.birthDate)
            order by year(c.birthDate), month(c.birthDate)
            """)
    List<MonthlyBirthCountView> countBirthsByYearAndMonth();
}
