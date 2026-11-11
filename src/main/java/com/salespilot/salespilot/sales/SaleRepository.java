package com.salespilot.salespilot.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByAgencyAndOrderRefAndProductSku(String agency, String orderRef, String productSku);

    @Query(value = """
            SELECT currency                                   AS "currency",
                   SUM(line_total)                            AS "revenue",
                   COUNT(DISTINCT CASE WHEN line_total > 0
                         THEN COALESCE(agency, '') || '|' || order_ref END) AS "invoiceCount",
                   SUM(quantity)                              AS "unitsSold"
            FROM sale
            WHERE order_date BETWEEN :from AND :to
            GROUP BY currency
            ORDER BY currency
            """, nativeQuery = true)
    List<CurrencyOverviewRow> overview(@Param("from") LocalDate from, @Param("to") LocalDate to);
}