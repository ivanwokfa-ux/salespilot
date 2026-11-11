package com.salespilot.salespilot.analytics;

import java.time.LocalDate;
import java.util.List;

public record MonthlyResponse(LocalDate from, LocalDate to, List<CurrencyMonthly> series) {
}