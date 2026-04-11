package com.fractalmindstudio.minerva_core.catalog.freeconcept.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConcept;
import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConceptRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FreeConceptRepositoryAdapter implements FreeConceptRepository {

    private final SpringDataFreeConceptRepository springDataFreeConceptRepository;
    private final SpringDataTaxRepository springDataTaxRepository;

    public FreeConceptRepositoryAdapter(
            final SpringDataFreeConceptRepository springDataFreeConceptRepository,
            final SpringDataTaxRepository springDataTaxRepository
    ) {
        this.springDataFreeConceptRepository = springDataFreeConceptRepository;
        this.springDataTaxRepository = springDataTaxRepository;
    }

    @Override
    public FreeConcept save(final FreeConcept freeConcept) {
        return toDomain(springDataFreeConceptRepository.save(toEntity(freeConcept)));
    }

    @Override
    public Optional<FreeConcept> findById(final UUID id) {
        return springDataFreeConceptRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<FreeConcept> findAll() {
        return springDataFreeConceptRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataFreeConceptRepository.deleteById(UuidMapper.toString(id));
    }

    private FreeConceptEntity toEntity(final FreeConcept freeConcept) {
        final FreeConceptEntity entity = new FreeConceptEntity();
        entity.setId(UuidMapper.toString(freeConcept.id()));
        entity.setName(freeConcept.name());
        entity.setBarcode(freeConcept.barcode());
        entity.setPrice(freeConcept.price());
        entity.setTax(springDataTaxRepository.getReferenceById(UuidMapper.toString(freeConcept.taxId())));
        entity.setDescription(freeConcept.description());
        return entity;
    }

    private FreeConcept toDomain(final FreeConceptEntity entity) {
        return new FreeConcept(
                UuidMapper.fromString(entity.getId()),
                entity.getName(),
                entity.getBarcode(),
                entity.getPrice(),
                entity.getTax() != null ? UuidMapper.fromString(entity.getTax().getId()) : null,
                entity.getDescription()
        );
    }
}
