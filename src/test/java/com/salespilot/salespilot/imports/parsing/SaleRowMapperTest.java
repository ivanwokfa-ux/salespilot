package com.salespilot.salespilot.imports.parsing;

import com.salespilot.salespilot.imports.ImportBatch;
import com.salespilot.salespilot.sales.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaleRowMapperTest {

    private final SaleRowMapper rowMapper = new SaleRowMapper(new ValueParser());
    private final ColumnMapping mapping = new ColumnMapper().map(List.of(
            "N° pièce", "Date", "Intitulé", "Référence article", "Désignation", "Qté", "Prix unitaire"));
    private final ImportBatch batch = new ImportBatch("ventes.csv", "hash");

    private RawRow row(String... values) {
        return new RawRow(2, List.of(values));
    }

    @Test
    void transformeUneLigneValideEnVente() {
        Sale sale = rowMapper.toSale(
                row("FYB04911", "06/01/26", "Client A", "01011450", "NEVADA 450 g", "120", "983,73"),
                mapping, batch, "XAF");

        assertThat(sale.getOrderRef()).isEqualTo("FYB04911");
        assertThat(sale.getOrderDate()).isEqualTo(LocalDate.of(2026, 1, 6));
        assertThat(sale.getCustomerName()).isEqualTo("Client A");
        assertThat(sale.getQuantity()).isEqualTo(120);
        assertThat(sale.getLineTotal()).isEqualByComparingTo("118047.60");
        assertThat(sale.getCurrency()).isEqualTo("XAF");
    }

    @Test
    void accepteUnAvoirEnQuantiteNegative() {
        Sale sale = rowMapper.toSale(
                row("FRYB0275", "13/01/26", "Client A", "01010125", "SAF INSTANT 125g", "-2", "447,70"),
                mapping, batch, "XAF");

        assertThat(sale.getLineTotal()).isEqualByComparingTo("-895.40");
    }

    @Test
    void rejetteUneLigneSansQuantite() {
        assertThatThrownBy(() -> rowMapper.toSale(
                row("FYB1", "06/01/26", "Client A", "01011450", "NEVADA 450 g", "", "983,73"),
                mapping, batch, "XAF"))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("quantite");
    }

    @Test
    void rejetteUneQuantiteNulle() {
        assertThatThrownBy(() -> rowMapper.toSale(
                row("FYB1", "06/01/26", "Client A", "01011450", "NEVADA 450 g", "0", "983,73"),
                mapping, batch, "XAF"))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void rejetteUneDeviseInvalide() {
        assertThatThrownBy(() -> rowMapper.toSale(
                row("FYB1", "06/01/26", "Client A", "01011450", "NEVADA 450 g", "1", "983,73"),
                mapping, batch, "FCFA"))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("Devise invalide");
    }
}