package com.fractalmindstudio.minerva_core.inventory.location.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository {

    Location save(Location location);

    Optional<Location> findById(UUID id);

    List<Location> findAll();

    void deleteById(UUID id);
}
