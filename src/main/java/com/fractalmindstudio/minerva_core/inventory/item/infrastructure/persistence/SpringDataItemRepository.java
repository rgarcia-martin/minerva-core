package com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataItemRepository extends JpaRepository<ItemEntity, String> {

    List<ItemEntity> findAllByOrderByIdAsc();

    List<ItemEntity> findAllByOriginPurchaseIdOrderByIdAsc(String originPurchaseId);

    void deleteAllByOriginPurchaseId(String originPurchaseId);
}
