package com.fractalmindstudio.minerva_core.purchasing.purchase.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository {

    Purchase save(Purchase purchase);

    Optional<Purchase> findById(UUID id);

    List<Purchase> findAll();

    void deleteById(UUID id);
}
