package com.salespilot.salespilot.analytics;

import com.salespilot.salespilot.common.error.BadRequestException;
import com.salespilot.salespilot.sales.CurrencyOverviewRow;
import com.salespilot.salespilot.sales.SaleRepository;
import com.salespilot.salespilot.sales.MonthlyRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


@Service
public class AnalyticsService {

    private final SaleRepository saleRepository;

    public AnalyticsService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public OverviewResponse overview(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("La date de debut doit preceder la date de fin");
        }
        List<CurrencyOverview> totals = saleRepository.overview(from, to).stream()
                .map(this::toCurrencyOverview)
                .toList();
        return new OverviewResponse(from, to, totals);
    }

    private CurrencyOverview toCurrencyOverview(CurrencyOverviewRow row) {
        long invoices = row.getInvoiceCount();
        BigDecimal averageBasket = invoices == 0
                ? null
                : row.getRevenue().divide(BigDecimal.valueOf(invoices), 2, RoundingMode.HALF_UP);
        return new CurrencyOverview(row.getCurrency(), row.getRevenue(), invoices, averageBasket, row.getUnitsSold());
    }
    @Transactional(readOnly = true)
    public MonthlyResponse monthly(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("La date de debut doit preceder la date de fin");
        }

        // Regroupe les lignes SQL par devise, puis par mois
        Map<String, Map<String, MonthlyRow>> byCurrency = new TreeMap<>();
        for (MonthlyRow row : saleRepository.monthly(from, to)) {
            byCurrency.computeIfAbsent(row.getCurrency(), c -> new HashMap<>()).put(row.getMonth(), row);
        }

        List<CurrencyMonthly> series = new ArrayList<>();
        for (Map.Entry<String, Map<String, MonthlyRow>> entry : byCurrency.entrySet()) {
            List<MonthlyPoint> points = new ArrayList<>();
            // Parcourt TOUS les mois de la periode, y compris ceux sans vente
            for (YearMonth month = YearMonth.from(from); !month.isAfter(YearMonth.from(to)); month = month.plusMonths(1)) {
                MonthlyRow row = entry.getValue().get(month.toString());
                points.add(row == null
                        ? new MonthlyPoint(month.toString(), BigDecimal.ZERO, 0)
                        : new MonthlyPoint(month.toString(), row.getRevenue(), row.getInvoiceCount()));
            }
            series.add(new CurrencyMonthly(entry.getKey(), points));
        }
        return new MonthlyResponse(from, to, series);
    }
}