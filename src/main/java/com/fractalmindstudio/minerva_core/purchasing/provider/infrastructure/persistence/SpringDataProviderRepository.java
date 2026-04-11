package com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataProviderRepository extends JpaRepository<ProviderEntity, String> {

    List<ProviderEntity> findAllByOrderByBusinessNameAsc();
}
