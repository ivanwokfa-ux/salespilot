package com.salespilot.salespilot.imports.parsing;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum SaleField {

    ORDER_REF("numero de piece", true,
            "N° pièce", "Numéro pièce", "Pièce", "N° facture", "Facture", "N° commande", "Commande", "order_ref"),
    ORDER_DATE("date", true,
            "Date", "Date facture", "Date de vente", "Date pièce", "order_date"),
    PRODUCT_SKU("reference article", true,
            "Référence article", "Référence", "Réf", "Code article", "Article", "SKU", "product_sku"),
    PRODUCT_NAME("designation", true,
            "Désignation", "Libellé", "Produit", "Nom article", "product_name"),
    QUANTITY("quantite", true,
            "Quantité", "Qté", "Qtés", "Qtés vendues", "Qty", "quantity"),
    UNIT_PRICE("prix unitaire", true,
            "Prix unitaire", "PU", "PU HT", "Prix unitaire HT", "Prix", "unit_price"),
    CURRENCY("devise", false,
            "Devise", "Monnaie", "currency"),
    CUSTOMER_CODE("code client", false,
            "N° client", "Code client", "Numéro client", "customer_code"),
    CUSTOMER_NAME("nom du client", false,
            "Intitulé", "Client", "Nom client", "Raison sociale", "customer_name"),
    AGENCY("agence", false,
            "Agence", "Site", "Magasin", "Point de vente", "agency"),
    COST_AMOUNT("cout", false,
            "Coût", "Coût total", "Prix de revient", "cost_amount");

    private final String label;
    private final boolean required;
    private final Set<String> synonyms;

    SaleField(String label, boolean required, String... synonyms) {
        this.label = label;
        this.required = required;
        this.synonyms = Arrays.stream(synonyms)
                .map(HeaderNormalizer::normalize)
                .collect(Collectors.toSet());
    }

    public boolean matches(String normalizedHeader) {
        return synonyms.contains(normalizedHeader);
    }

    public String getLabel() { return label; }
    public boolean isRequired() { return required; }
}