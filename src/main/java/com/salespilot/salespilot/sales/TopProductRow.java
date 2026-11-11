package com.salespilot.salespilot.sales;

import java.math.BigDecimal;

public interface TopProductRow {
    String getCurrency();
    Long getRank();
    String getProductSku();
    String getProductName();
    Long getUnitsSold();
    BigDecimal getRevenue();
    BigDecimal getSharePercent();
}