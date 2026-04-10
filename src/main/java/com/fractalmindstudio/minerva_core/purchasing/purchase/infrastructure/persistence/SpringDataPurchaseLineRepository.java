package com.fractalmindstudio.minerva_core.purchasing.purchase.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPurchaseLineRepository extends JpaRepository<PurchaseLineEntity, String> {
}
