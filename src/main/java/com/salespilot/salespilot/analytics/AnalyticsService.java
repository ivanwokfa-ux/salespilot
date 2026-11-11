package com.salespilot.salespilot.analytics;

import com.salespilot.salespilot.common.error.BadRequestException;
import com.salespilot.salespilot.sales.CurrencyOverviewRow;
import com.salespilot.salespilot.sales.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

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
}