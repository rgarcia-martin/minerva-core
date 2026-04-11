package com.fractalmindstudio.minerva_core.catalog.tax.application;

import com.fractalmindstudio.minerva_core.catalog.tax.domain.Tax;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TaxService {

    public static final String RESOURCE_NAME = "tax";

    private final TaxRepository taxRepository;

    public TaxService(final TaxRepository taxRepository) {
        this.taxRepository = taxRepository;
    }

    @Transactional
    public Tax create(
            final String description,
            final BigDecimal rate,
            final BigDecimal surchargeRate
    ) {
        return taxRepository.save(Tax.create(description, rate, surchargeRate));
    }

    public Tax getById(final UUID id) {
        return taxRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Tax> findAll() {
        return taxRepository.findAll();
    }

    @Transactional
    public Tax update(
            final UUID id,
            final String description,
            final BigDecimal rate,
            final BigDecimal surchargeRate
    ) {
        getById(id);
        return taxRepository.save(new Tax(id, description, rate, surchargeRate));
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        taxRepository.deleteById(id);
    }
}
