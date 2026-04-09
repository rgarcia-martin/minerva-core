package com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataItemRepository extends JpaRepository<ItemEntity, String> {
}
