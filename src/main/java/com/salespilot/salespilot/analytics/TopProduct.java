package com.salespilot.salespilot.analytics;

import java.math.BigDecimal;

public record TopProduct(String currency, long rank, String productSku, String productName,
                         long unitsSold, BigDecimal revenue, BigDecimal sharePercent) {
}