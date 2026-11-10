package com.salespilot.salespilot.sales;

import com.salespilot.salespilot.imports.ImportBatch;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "sale")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_batch_id", nullable = false)
    private ImportBatch importBatch;

    @Column(name = "order_ref", nullable = false, length = 64)
    private String orderRef;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "product_sku", nullable = false, length = 64)
    private String productSku;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "customer_code", length = 64)
    private String customerCode;

    @Column(name = "customer_name")
    private String customerName;

    @Column(length = 64)
    private String agency;

    @Column(name = "cost_amount", precision = 14, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Sale() {
    }

    public Sale(ImportBatch importBatch, String orderRef, LocalDate orderDate,
                String productSku, String productName, int quantity,
                BigDecimal unitPrice, String currency) {
        if (quantity == 0) {
            throw new IllegalArgumentException("La quantite ne peut pas etre nulle");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas etre negatif");
        }
        this.importBatch = importBatch;
        this.orderRef = orderRef;
        this.orderDate = orderDate;
        this.productSku = productSku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency.toUpperCase();
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Sale withCustomer(String code, String name) {
        this.customerCode = code;
        this.customerName = name;
        return this;
    }

    public Sale withAgency(String agency) {
        this.agency = agency;
        return this;
    }

    public Sale withCost(BigDecimal costAmount) {
        this.costAmount = costAmount;
        return this;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public ImportBatch getImportBatch() { return importBatch; }
    public String getOrderRef() { return orderRef; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getProductSku() { return productSku; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public String getCurrency() { return currency; }
    public String getCustomerCode() { return customerCode; }
    public String getCustomerName() { return customerName; }
    public String getAgency() { return agency; }
    public BigDecimal getCostAmount() { return costAmount; }
    public Instant getCreatedAt() { return createdAt; }
}