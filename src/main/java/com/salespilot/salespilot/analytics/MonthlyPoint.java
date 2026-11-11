package com.salespilot.salespilot.analytics;

import java.math.BigDecimal;

public record MonthlyPoint(String month, BigDecimal revenue, long invoiceCount) {
}