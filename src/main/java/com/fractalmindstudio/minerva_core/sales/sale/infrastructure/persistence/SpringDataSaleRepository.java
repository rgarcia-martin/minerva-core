package com.fractalmindstudio.minerva_core.sales.sale.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSaleRepository extends JpaRepository<SaleEntity, String> {

    List<SaleEntity> findAllByOrderByCreatedOnDesc();
}
