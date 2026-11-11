package com.salespilot.salespilot.sales;

import java.math.BigDecimal;

/** Une ligne du resultat SQL de la synthese : une par devise. */
public interface CurrencyOverviewRow {
    String getCurrency();
    BigDecimal getRevenue();
    Long getInvoiceCount();
    Long getUnitsSold();
}