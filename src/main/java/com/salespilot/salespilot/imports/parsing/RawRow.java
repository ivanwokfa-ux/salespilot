package com.salespilot.salespilot.imports.parsing;

import java.util.List;

public record RawRow(int lineNumber, List<String> values) {
}