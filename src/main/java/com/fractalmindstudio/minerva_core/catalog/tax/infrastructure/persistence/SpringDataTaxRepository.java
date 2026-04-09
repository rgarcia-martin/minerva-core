package com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTaxRepository extends JpaRepository<TaxEntity, String> {
}
