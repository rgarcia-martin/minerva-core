package com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.tax.domain.Tax;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TaxRepositoryAdapter implements TaxRepository {

    private final SpringDataTaxRepository springDataTaxRepository;

    public TaxRepositoryAdapter(final SpringDataTaxRepository springDataTaxRepository) {
        this.springDataTaxRepository = springDataTaxRepository;
    }

    @Override
    public Tax save(final Tax tax) {
        final TaxEntity entity = toEntity(tax);
        final TaxEntity savedEntity = springDataTaxRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Tax> findById(final UUID id) {
        return springDataTaxRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<Tax> findAll() {
        return springDataTaxRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataTaxRepository.deleteById(UuidMapper.toString(id));
    }

    private TaxEntity toEntity(final Tax tax) {
        final TaxEntity entity = new TaxEntity();
        entity.setId(UuidMapper.toString(tax.id()));
        entity.setDescription(tax.description());
        entity.setRate(tax.rate());
        entity.setSurchargeRate(tax.surchargeRate());
        return entity;
    }

    private Tax toDomain(final TaxEntity entity) {
        return new Tax(
                UuidMapper.fromString(entity.getId()),
                entity.getDescription(),
                entity.getRate(),
                entity.getSurchargeRate()
        );
    }
}
