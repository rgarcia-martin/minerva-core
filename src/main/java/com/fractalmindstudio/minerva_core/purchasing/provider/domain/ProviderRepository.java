package com.fractalmindstudio.minerva_core.purchasing.provider.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository {

    Provider save(Provider provider);

    Optional<Provider> findById(UUID id);

    List<Provider> findAll();

    void deleteById(UUID id);
}
