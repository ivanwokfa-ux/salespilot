package com.salespilot.salespilot.imports;

import com.salespilot.salespilot.common.error.ResourceNotFoundException;

public class ImportNotFoundException extends ResourceNotFoundException {

    public ImportNotFoundException(Long id) {
        super("Import " + id + " introuvable");
    }
}