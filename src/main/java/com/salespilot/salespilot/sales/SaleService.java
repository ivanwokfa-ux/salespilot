package com.salespilot.salespilot.sales;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salespilot.salespilot.common.error.BadRequestException;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public Page<SaleResponse> search(Long importId, String agency, LocalDate from, LocalDate to,
                                     String text, Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("La date de début doit précéder la date de fin");
        }

        // Point de depart : une condition toujours vraie (aucun filtre)
        Specification<Sale> spec = (root, query, cb) -> cb.conjunction();

        // On n'ajoute que les filtres reellement demandes
        if (importId != null) {
            spec = spec.and(SaleSpecifications.fromImport(importId));
        }
        if (agency != null && !agency.isBlank()) {
            spec = spec.and(SaleSpecifications.agency(agency.trim()));
        }
        if (from != null) {
            spec = spec.and(SaleSpecifications.orderedFrom(from));
        }
        if (to != null) {
            spec = spec.and(SaleSpecifications.orderedTo(to));
        }
        if (text != null && !text.isBlank()) {
            spec = spec.and(SaleSpecifications.matches(text.trim()));
        }

        return saleRepository.findAll(spec, pageable).map(SaleResponse::from);
    }
}