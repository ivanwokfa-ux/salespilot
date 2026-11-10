package com.salespilot.salespilot.imports.parsing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

@Component
public class ValueParser {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            "uuuu-MM-dd", "dd/MM/uuuu", "dd/MM/uu", "dd-MM-uuuu", "dd.MM.uuuu"
    ).stream().map(p -> DateTimeFormatter.ofPattern(p).withResolverStyle(ResolverStyle.STRICT)).toList();

    public LocalDate parseDate(String value) {
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value.trim(), format);
            } catch (DateTimeParseException ignored) {
                // on essaie le format suivant
            }
        }
        throw new InvalidValueException("Date invalide : '" + value + "'");
    }

    public BigDecimal parseDecimal(String value) {
        // Retire les espaces (y compris insecables) et le symbole euro
        String cleaned = value.replaceAll("[\\s\\u00A0\\u202F€]", "");
        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');

        if (lastComma >= 0 && lastDot >= 0) {
            // Les deux sont presents : le dernier est le separateur decimal
            cleaned = lastComma > lastDot
                    ? cleaned.replace(".", "").replace(',', '.')
                    : cleaned.replace(",", "");
        } else if (lastComma >= 0) {
            cleaned = cleaned.replace(',', '.');
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new InvalidValueException("Nombre invalide : '" + value + "'");
        }
    }

    public int parseQuantity(String value) {
        BigDecimal number = parseDecimal(value);
        try {
            return number.stripTrailingZeros().intValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidValueException("Quantite non entiere : '" + value + "'");
        }
    }
}