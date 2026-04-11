package com.fractalmindstudio.minerva_core.catalog.freeconcept.interfaces.rest;

import com.fractalmindstudio.minerva_core.catalog.freeconcept.application.FreeConceptService;
import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConcept;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(FreeConceptController.BASE_PATH)
@Validated
public class FreeConceptController {

    public static final String BASE_PATH = "/api/v1/free-concepts";

    private final FreeConceptService freeConceptService;

    public FreeConceptController(final FreeConceptService freeConceptService) {
        this.freeConceptService = freeConceptService;
    }

    @PostMapping
    public ResponseEntity<FreeConceptResponse> create(@Valid @RequestBody final UpsertFreeConceptRequest request) {
        final FreeConcept freeConcept = freeConceptService.create(
                request.name(), request.barcode(), request.price(), request.taxId(), request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(freeConcept));
    }

    @GetMapping
    public List<FreeConceptResponse> findAll() {
        return freeConceptService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{freeConceptId}")
    public FreeConceptResponse getById(@PathVariable final UUID freeConceptId) {
        return toResponse(freeConceptService.getById(freeConceptId));
    }

    @PutMapping("/{freeConceptId}")
    public FreeConceptResponse update(
            @PathVariable final UUID freeConceptId,
            @Valid @RequestBody final UpsertFreeConceptRequest request
    ) {
        final FreeConcept freeConcept = freeConceptService.update(
                freeConceptId, request.name(), request.barcode(), request.price(), request.taxId(), request.description()
        );
        return toResponse(freeConcept);
    }

    @DeleteMapping("/{freeConceptId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID freeConceptId) {
        freeConceptService.delete(freeConceptId);
        return ResponseEntity.noContent().build();
    }

    private FreeConceptResponse toResponse(final FreeConcept freeConcept) {
        return new FreeConceptResponse(
                freeConcept.id(), freeConcept.name(), freeConcept.barcode(),
                freeConcept.price(), freeConcept.taxId(), freeConcept.description()
        );
    }

    public record UpsertFreeConceptRequest(
            @NotBlank String name,
            @NotBlank String barcode,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @NotNull UUID taxId,
            String description
    ) {
    }

    public record FreeConceptResponse(
            UUID id,
            String name,
            String barcode,
            BigDecimal price,
            UUID taxId,
            String description
    ) {
    }
}
