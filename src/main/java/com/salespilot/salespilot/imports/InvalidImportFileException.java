package com.salespilot.salespilot.imports;

import com.salespilot.salespilot.common.error.BadRequestException;

public class InvalidImportFileException extends BadRequestException {

    public InvalidImportFileException(String message) {
        super(message);
    }
}