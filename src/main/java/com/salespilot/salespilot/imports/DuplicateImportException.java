package com.salespilot.salespilot.imports;

import com.salespilot.salespilot.common.error.ConflictException;

public class DuplicateImportException extends ConflictException {

    public DuplicateImportException(String fileName) {
        super("Le fichier " + fileName + " a deja ete importe");
    }
}