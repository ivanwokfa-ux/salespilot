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
    @Query(value = """
            SELECT currency                                            AS "currency",
                   to_char(date_trunc('month', order_date), 'YYYY-MM') AS "month",
                   SUM(line_total)                                     AS "revenue",
                   COUNT(DISTINCT CASE WHEN line_total > 0
                         THEN COALESCE(agency, '') || '|' || order_ref END) AS "invoiceCount"
            FROM sale
            WHERE order_date BETWEEN :from AND :to
            GROUP BY currency, date_trunc('month', order_date)
            ORDER BY currency, date_trunc('month', order_date)
            """, nativeQuery = true)
    List<MonthlyRow> monthly(@Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query(value = """
            SELECT currency     AS "currency",
                   rank         AS "rank",
                   product_sku  AS "productSku",
                   product_name AS "productName",
                   units        AS "unitsSold",
                   revenue      AS "revenue",
                   share        AS "sharePercent"
            FROM (
                SELECT currency,
                       product_sku,
                       MAX(product_name) AS product_name,
                       SUM(quantity)     AS units,
                       SUM(line_total)   AS revenue,
                       ROUND(100 * SUM(line_total)
                             / NULLIF(SUM(SUM(line_total)) OVER (PARTITION BY currency), 0), 2) AS share,
                       ROW_NUMBER() OVER (PARTITION BY currency ORDER BY SUM(line_total) DESC) AS rank
                FROM sale
                WHERE order_date BETWEEN :from AND :to
                GROUP BY currency, product_sku
            ) ranked
            WHERE rank <= :limit
            ORDER BY currency, rank
            """, nativeQuery = true)
    List<TopProductRow> topProducts(@Param("from") LocalDate from,
                                    @Param("to") LocalDate to,
                                    @Param("limit") int limit);
}