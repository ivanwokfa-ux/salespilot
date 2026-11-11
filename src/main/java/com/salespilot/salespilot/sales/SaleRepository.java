package com.salespilot.salespilot.sales;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByAgencyAndOrderRefAndProductSku(String agency, String orderRef, String productSku);
}