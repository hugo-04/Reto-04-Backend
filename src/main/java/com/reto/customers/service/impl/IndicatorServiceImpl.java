package com.reto.customers.service.impl;

import com.reto.customers.dto.response.BirthIndicatorsResponse;
import com.reto.customers.dto.MonthlyBirthStat;
import com.reto.customers.exception.IndicatorCalculationException;
import com.reto.customers.repository.CustomerRepository;
import com.reto.customers.repository.projection.MonthlyBirthCountView;
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

    private final CustomerRepository repository;

    @Override
    public BirthIndicatorsResponse calculate() {
        List<MonthlyBirthCountView> counts;
        try {
            counts = repository.countBirthsByYearAndMonth();
        } catch (DataAccessException ex) {
            throw new IndicatorCalculationException("No se pudieron calcular los indicadores", ex);
        }

        if (counts.isEmpty()) {
            return new BirthIndicatorsResponse(0, List.of(), null, null);
        }

        long total = counts.stream().mapToLong(MonthlyBirthCountView::getTotal).sum();
        BigDecimal divisor = BigDecimal.valueOf(total);

        List<MonthlyBirthStat> stats =
                counts.stream()
                .map(c -> new MonthlyBirthStat(
                        c.getBirthYear(),
                        c.getBirthMonth(),
                        c.getTotal(),
                        BigDecimal.valueOf(c.getTotal() * 100).divide(divisor, 2, RoundingMode.HALF_UP)))
                .toList();

        // La consulta viene ordenada por fecha: en empate, max/min conservan el mes más antiguo
        Comparator<MonthlyBirthStat> byCount = Comparator.comparingLong(MonthlyBirthStat::count);

        return new BirthIndicatorsResponse(total, stats,
                stats.stream().max(byCount).orElseThrow(),
                stats.stream().min(byCount).orElseThrow());
    }
}
