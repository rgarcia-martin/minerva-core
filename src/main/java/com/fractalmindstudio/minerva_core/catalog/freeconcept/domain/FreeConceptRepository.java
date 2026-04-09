package com.fractalmindstudio.minerva_core.catalog.freeconcept.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FreeConceptRepository {
    FreeConcept save(FreeConcept freeConcept);

    Optional<FreeConcept> findById(UUID id);

    List<FreeConcept> findAll();

    void deleteById(UUID id);
}
