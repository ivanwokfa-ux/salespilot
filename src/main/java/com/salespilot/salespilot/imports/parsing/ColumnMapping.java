package com.salespilot.salespilot.imports.parsing;

import java.util.Map;

public record ColumnMapping(Map<SaleField, Integer> positions) {

    /** La valeur du champ sur cette ligne, ou null si la colonne est absente ou la case vide. */
    public String valueOf(RawRow row, SaleField field) {
        Integer index = positions.get(field);
        if (index == null || index >= row.values().size()) {
            return null;
        }
        String value = row.values().get(index);
        return value.isBlank() ? null : value;
    }

    public boolean has(SaleField field) {
        return positions.containsKey(field);
    }
}