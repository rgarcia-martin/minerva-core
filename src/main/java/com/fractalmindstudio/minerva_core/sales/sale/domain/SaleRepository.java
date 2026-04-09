package com.fractalmindstudio.minerva_core.sales.sale.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository {

    Sale save(Sale sale);

    Optional<Sale> findById(UUID id);

    List<Sale> findAll();

    void deleteById(UUID id);
}
