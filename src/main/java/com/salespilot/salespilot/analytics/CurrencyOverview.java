package com.salespilot.salespilot.analytics;

import java.math.BigDecimal;

public record CurrencyOverview(
        String currency,
        BigDecimal revenue,
        long invoiceCount,
        BigDecimal averageBasket,
        long unitsSold
) {
}