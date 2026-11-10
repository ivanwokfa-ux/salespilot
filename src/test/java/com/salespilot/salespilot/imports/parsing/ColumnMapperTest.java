package com.salespilot.salespilot.imports.parsing;

import com.salespilot.salespilot.imports.InvalidImportFileException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColumnMapperTest {

    private final ColumnMapper mapper = new ColumnMapper();

    @Test
    void reconnaitLesColonnesDeSage() {
        List<String> headers = List.of("N° pièce", "Date", "N° client", "Intitulé",
                "Référence article", "Désignation", "Qtés vendues", "Prix unitaire", "Échéance");

        ColumnMapping mapping = mapper.map(headers);

        assertThat(mapping.positions())
                .containsEntry(SaleField.ORDER_REF, 0)
                .containsEntry(SaleField.ORDER_DATE, 1)
                .containsEntry(SaleField.CUSTOMER_CODE, 2)
                .containsEntry(SaleField.CUSTOMER_NAME, 3)
                .containsEntry(SaleField.PRODUCT_SKU, 4)
                .containsEntry(SaleField.PRODUCT_NAME, 5)
                .containsEntry(SaleField.QUANTITY, 6)
                .containsEntry(SaleField.UNIT_PRICE, 7)
                .hasSize(8);
    }

    @Test
    void reconnaitLeFormatDeDemonstration() {
        List<String> headers = List.of("order_ref", "order_date", "product_sku", "product_name", "quantity", "unit_price");

        assertThat(mapper.map(headers).positions()).hasSize(6);
    }

    @Test
    void ignoreLaCasseEtLesAccents() {
        List<String> headers = List.of("PIECE", "DATE", "REFERENCE", "DESIGNATION", "QTE", "PU HT");

        assertThat(mapper.map(headers).positions()).hasSize(6);
    }

    @Test
    void signaleLesColonnesObligatoiresManquantes() {
        List<String> headers = List.of("Pièce", "Date", "Référence", "Désignation");

        assertThatThrownBy(() -> mapper.map(headers))
                .isInstanceOf(InvalidImportFileException.class)
                .hasMessageContaining("quantite")
                .hasMessageContaining("prix unitaire");
    }
}