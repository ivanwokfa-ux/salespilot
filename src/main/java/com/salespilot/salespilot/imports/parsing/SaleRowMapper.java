package com.salespilot.salespilot.imports.parsing;

import com.salespilot.salespilot.imports.ImportBatch;
import com.salespilot.salespilot.sales.Sale;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class SaleRowMapper {

    private final ValueParser parser;

    public SaleRowMapper(ValueParser parser) {
        this.parser = parser;
    }

    /**
     * Transforme une ligne du fichier en vente.
     * Leve InvalidValueException avec la raison du rejet si la ligne est invalide.
     */
    public Sale toSale(RawRow row, ColumnMapping mapping, ImportBatch batch, String defaultCurrency) {
        String orderRef = required(row, mapping, SaleField.ORDER_REF, 64);
        LocalDate orderDate = parser.parseDate(required(row, mapping, SaleField.ORDER_DATE, 64));
        String productSku = required(row, mapping, SaleField.PRODUCT_SKU, 64);
        String productName = required(row, mapping, SaleField.PRODUCT_NAME, 255);
        int quantity = parser.parseQuantity(required(row, mapping, SaleField.QUANTITY, 64));
        BigDecimal unitPrice = parser.parseDecimal(required(row, mapping, SaleField.UNIT_PRICE, 64));
        String currency = currency(row, mapping, defaultCurrency);

        Sale sale;
        try {
            sale = new Sale(batch, orderRef, orderDate, productSku, productName, quantity, unitPrice, currency);
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException(e.getMessage());
        }

        sale.withCustomer(optional(row, mapping, SaleField.CUSTOMER_CODE, 64),
                        optional(row, mapping, SaleField.CUSTOMER_NAME, 255))
                .withAgency(optional(row, mapping, SaleField.AGENCY, 64));

        String cost = mapping.valueOf(row, SaleField.COST_AMOUNT);
        if (cost != null) {
            sale.withCost(parser.parseDecimal(cost));
        }
        return sale;
    }

    private String required(RawRow row, ColumnMapping mapping, SaleField field, int maxLength) {
        String value = mapping.valueOf(row, field);
        if (value == null) {
            throw new InvalidValueException("Champ obligatoire manquant : " + field.getLabel());
        }
        return checkLength(value, field, maxLength);
    }

    private String optional(RawRow row, ColumnMapping mapping, SaleField field, int maxLength) {
        String value = mapping.valueOf(row, field);
        return value == null ? null : checkLength(value, field, maxLength);
    }

    private String checkLength(String value, SaleField field, int maxLength) {
        if (value.length() > maxLength) {
            throw new InvalidValueException(field.getLabel() + " trop long (" + maxLength + " caracteres maximum)");
        }
        return value;
    }

    private String currency(RawRow row, ColumnMapping mapping, String defaultCurrency) {
        String value = mapping.valueOf(row, SaleField.CURRENCY);
        String currency = (value != null ? value : defaultCurrency).trim().toUpperCase();
        if (!currency.matches("[A-Z]{3}")) {
            throw new InvalidValueException("Devise invalide : '" + currency + "' (attendu : 3 lettres, ex. XAF)");
        }
        return currency;
    }
}