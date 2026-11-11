package com.salespilot.salespilot.analytics;

import java.util.List;

public record CurrencyMonthly(String currency, List<MonthlyPoint> months) {
}