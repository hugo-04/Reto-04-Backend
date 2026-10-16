package com.reto.customers.service;

import com.reto.customers.dto.response.BirthIndicatorsResponse;
import com.reto.customers.projection.MonthlyBirthCountView;
import com.reto.customers.repository.CustomerRepository;
import com.reto.customers.service.impl.IndicatorServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicatorServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private IndicatorServiceImpl service;

    @Test
    @DisplayName("calculate: obtiene mayor, menor y tasa de natalidad por mes")
    void calculateDevuelveMayorMenorYTasa() {
        // 5 clientes en total: 3 en mayo/1990 y uno en marzo/1985 y julio/1992
        when(repository.countBirthsByYearAndMonth()).thenReturn(List.of(
                view(1985, 3, 1L),
                view(1990, 5, 3L),
                view(1992, 7, 1L)));

        BirthIndicatorsResponse result = service.calculate();

        assertThat(result.totalCustomers()).isEqualTo(5);
        assertThat(result.birthsByMonth()).hasSize(3);
        assertThat(result.highest().year()).isEqualTo(1990);
        assertThat(result.highest().month()).isEqualTo(5);
        assertThat(result.highest().birthRate()).isEqualByComparingTo(new BigDecimal("60.00"));
        // Empate entre 1985-03 y 1992-07: se conserva el más antiguo
        assertThat(result.lowest().year()).isEqualTo(1985);
        assertThat(result.lowest().birthRate()).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    /** Implementación mínima de la proyección para los datos de prueba. */
    private MonthlyBirthCountView view(int year, int month, long total) {
        return new MonthlyBirthCountView() {
            @Override
            public Integer getBirthYear() {
                return year;
            }

            @Override
            public Integer getBirthMonth() {
                return month;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }
}
