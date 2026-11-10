package com.salespilot.salespilot.imports.parsing;

import java.text.Normalizer;
import java.util.Locale;

public final class HeaderNormalizer {

    private HeaderNormalizer() {
    }

    /** "N° Pièce" -> "n piece", "order_ref" -> "order ref", "Qtés vendues" -> "qtes vendues" */
    public static String normalize(String header) {
        String withoutAccents = Normalizer.normalize(header, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }
}