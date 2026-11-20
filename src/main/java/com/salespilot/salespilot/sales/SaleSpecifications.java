package com.salespilot.salespilot.sales;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

/**
 * Les briques de filtre pour rechercher des ventes.
 * Chaque methode renvoie une condition ; le service n'assemble que celles demandees.
 */
public final class SaleSpecifications {

    private SaleSpecifications() {
    }

    public static Specification<Sale> fromImport(Long importBatchId) {
        return (root, query, cb) -> cb.equal(root.get("importBatch").get("id"), importBatchId);
    }

    public static Specification<Sale> agency(String agency) {
        return (root, query, cb) -> cb.equal(cb.upper(root.get("agency")), agency.toUpperCase(Locale.ROOT));
    }

    public static Specification<Sale> orderedFrom(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), from);
    }

    public static Specification<Sale> orderedTo(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), to);
    }

    /** Recherche libre dans le produit, la reference, le n° de piece et le client. */
    public static Specification<Sale> matches(String text) {
        String pattern = "%" + text.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("productName")), pattern),
                cb.like(cb.lower(root.get("productSku")), pattern),
                cb.like(cb.lower(root.get("orderRef")), pattern),
                cb.like(cb.lower(root.get("customerName")), pattern));
    }
}