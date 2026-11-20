package com.salespilot.salespilot.sales;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaleResponse(
        Long id,
        Long importBatchId,
        String orderRef,
        LocalDate orderDate,
        String agency,
        String customerCode,
        String customerName,
        String productSku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String currency) {

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getImportBatch().getId(),
                sale.getOrderRef(),
                sale.getOrderDate(),
                sale.getAgency(),
                sale.getCustomerCode(),
                sale.getCustomerName(),
                sale.getProductSku(),
                sale.getProductName(),
                sale.getQuantity(),
                sale.getUnitPrice(),
                sale.getLineTotal(),
                sale.getCurrency());
    }
}