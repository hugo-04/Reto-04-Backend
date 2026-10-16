package com.reto.customers.service.impl;

import com.reto.customers.dto.response.BirthIndicatorsResponse;
import com.reto.customers.dto.MonthlyBirthStat;
import com.reto.customers.exception.IndicatorCalculationException;
import com.reto.customers.projection.MonthlyBirthCountView;
import com.reto.customers.repository.CustomerRepository;
import com.reto.customers.service.IndicatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorServiceImpl implements IndicatorService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final CustomerRepository repository;


    @Override
    public BirthIndicatorsResponse calculate() {
        List<MonthlyBirthCountView> counts;
        try {
            counts = repository.countBirthsByYearAndMonth();
        } catch (DataAccessException ex) {
            log.error("Fallo al consultar nacimientos por mes/año", ex);
            throw new IndicatorCalculationException("No se pudieron calcular los indicadores", ex);
        }

        long total = counts.stream().mapToLong(MonthlyBirthCountView::getTotal).sum();
        if (total == 0) {
            return new BirthIndicatorsResponse(0, List.of(), null, null);
        }

        List<MonthlyBirthStat> stats = counts.stream()
                .map(c -> new MonthlyBirthStat(
                        c.getBirthYear(), c.getBirthMonth(), c.getTotal(), birthRate(c.getTotal(), total)))
                .toList();

        // max/min conservan el primer elemento en caso de empate (la lista viene ordenada por fecha)
        MonthlyBirthStat highest = stats.stream().max(Comparator.comparingLong(MonthlyBirthStat::count)).orElse(null);
        MonthlyBirthStat lowest = stats.stream().min(Comparator.comparingLong(MonthlyBirthStat::count)).orElse(null);

        return new BirthIndicatorsResponse(total, stats, highest, lowest);
    }

    /** Tasa de natalidad = (clientes del mes / total de clientes) * 100, con 2 decimales. */
    private BigDecimal birthRate(long monthCount, long total) {
        return BigDecimal.valueOf(monthCount)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
