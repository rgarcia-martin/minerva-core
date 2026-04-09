package com.fractalmindstudio.minerva_core.inventory.item.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

    Item save(Item item);

    Optional<Item> findById(UUID id);

    List<Item> findAll();

    void deleteById(UUID id);
}
