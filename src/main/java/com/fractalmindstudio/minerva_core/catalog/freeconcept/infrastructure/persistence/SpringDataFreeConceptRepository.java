package com.fractalmindstudio.minerva_core.catalog.freeconcept.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFreeConceptRepository extends JpaRepository<FreeConceptEntity, String> {
}
