package com.salespilot.salespilot.imports.parsing;

import com.salespilot.salespilot.imports.InvalidImportFileException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ColumnMapper {

    public ColumnMapping map(List<String> headers) {
        Map<SaleField, Integer> positions = new EnumMap<>(SaleField.class);

        for (int i = 0; i < headers.size(); i++) {
            String header = HeaderNormalizer.normalize(headers.get(i));
            for (SaleField field : SaleField.values()) {
                if (!positions.containsKey(field) && field.matches(header)) {
                    positions.put(field, i);
                    break;
                }
            }
        }

        List<String> missing = Arrays.stream(SaleField.values())
                .filter(SaleField::isRequired)
                .filter(field -> !positions.containsKey(field))
                .map(SaleField::getLabel)
                .toList();
        if (!missing.isEmpty()) {
            throw new InvalidImportFileException("Colonnes obligatoires introuvables : " + String.join(", ", missing));
        }
        return new ColumnMapping(positions);
    }
}