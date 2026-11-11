package com.salespilot.salespilot.sales;

import java.math.BigDecimal;

public interface MonthlyRow {
    String getCurrency();
    String getMonth();
    BigDecimal getRevenue();
    Long getInvoiceCount();
}