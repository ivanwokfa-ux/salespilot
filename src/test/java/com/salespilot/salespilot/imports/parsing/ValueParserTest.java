package com.salespilot.salespilot.imports.parsing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueParserTest {

    private final ValueParser parser = new ValueParser();

    @Test
    void litLesDatesDansPlusieursFormats() {
        LocalDate expected = LocalDate.of(2026, 1, 31);
        assertThat(parser.parseDate("2026-01-31")).isEqualTo(expected);
        assertThat(parser.parseDate("31/01/2026")).isEqualTo(expected);
        assertThat(parser.parseDate("31/01/26")).isEqualTo(expected);
        assertThat(parser.parseDate("31-01-2026")).isEqualTo(expected);
    }

    @Test
    void refuseLesDatesImpossibles() {
        assertThatThrownBy(() -> parser.parseDate("31/02/2026")).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> parser.parseDate("2026-10-32")).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> parser.parseDate("hier")).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void litLesNombresFrancaisEtAnglais() {
        assertThat(parser.parseDecimal("19.90")).isEqualByComparingTo("19.90");
        assertThat(parser.parseDecimal("19,90")).isEqualByComparingTo("19.90");
        assertThat(parser.parseDecimal("1 449 500")).isEqualByComparingTo("1449500");
        assertThat(parser.parseDecimal("1\u00A0234,56")).isEqualByComparingTo("1234.56");
        assertThat(parser.parseDecimal("1,234.56")).isEqualByComparingTo("1234.56");
        assertThat(parser.parseDecimal("-59,00")).isEqualByComparingTo("-59");
        assertThat(parser.parseDecimal("12,50 €")).isEqualByComparingTo("12.50");
    }

    @Test
    void refuseLesNombresInvalides() {
        assertThatThrownBy(() -> parser.parseDecimal("deux")).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void litLesQuantitesEntieres() {
        assertThat(parser.parseQuantity("3")).isEqualTo(3);
        assertThat(parser.parseQuantity("3,00")).isEqualTo(3);
        assertThat(parser.parseQuantity("-2")).isEqualTo(-2);
        assertThatThrownBy(() -> parser.parseQuantity("2,5")).isInstanceOf(InvalidValueException.class);
    }
}