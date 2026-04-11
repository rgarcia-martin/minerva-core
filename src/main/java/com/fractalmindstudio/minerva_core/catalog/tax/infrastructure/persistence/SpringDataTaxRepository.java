package com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataTaxRepository extends JpaRepository<TaxEntity, String> {

    List<TaxEntity> findAllByOrderByDescriptionAsc();
}
