package com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataLocationRepository extends JpaRepository<LocationEntity, String> {

    List<LocationEntity> findAllByOrderByNameAsc();
}
