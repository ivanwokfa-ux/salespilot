package com.salespilot.salespilot.imports;

public record ImportErrorResponse(int lineNumber, String rawLine, String errorMessage) {

    public static ImportErrorResponse from(ImportError error) {
        return new ImportErrorResponse(error.getLineNumber(), error.getRawLine(), error.getErrorMessage());
    }
}