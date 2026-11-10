package com.salespilot.salespilot.imports.parsing;

import java.util.List;

public record RawTable(List<String> headers, List<RawRow> rows) {
}