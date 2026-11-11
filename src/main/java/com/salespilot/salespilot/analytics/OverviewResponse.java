package com.salespilot.salespilot.analytics;

import java.time.LocalDate;
import java.util.List;

public record OverviewResponse(LocalDate from, LocalDate to, List<CurrencyOverview> totals) {
}