package com.fractalmindstudio.minerva_core.catalog.freeconcept.application;

import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConcept;
import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConceptRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FreeConceptService {

    public static final String RESOURCE_NAME = "freeConcept";

    private final FreeConceptRepository freeConceptRepository;

    public FreeConceptService(final FreeConceptRepository freeConceptRepository) {
        this.freeConceptRepository = freeConceptRepository;
    }

    @Transactional
    public FreeConcept create(
            final String name,
            final String barcode,
            final BigDecimal price,
            final UUID taxId,
            final String description
    ) {
        return freeConceptRepository.save(FreeConcept.create(name, barcode, price, taxId, description));
    }

    public FreeConcept getById(final UUID id) {
        return freeConceptRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<FreeConcept> findAll() {
        return freeConceptRepository.findAll().stream()
                .sorted(Comparator.comparing(FreeConcept::name))
                .toList();
    }

    @Transactional
    public FreeConcept update(
            final UUID id,
            final String name,
            final String barcode,
            final BigDecimal price,
            final UUID taxId,
            final String description
    ) {
        getById(id);
        return freeConceptRepository.save(new FreeConcept(id, name, barcode, price, taxId, description));
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        freeConceptRepository.deleteById(id);
    }
}
