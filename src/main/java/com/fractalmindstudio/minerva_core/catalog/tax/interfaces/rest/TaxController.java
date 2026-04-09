package com.fractalmindstudio.minerva_core.catalog.tax.interfaces.rest;

import com.fractalmindstudio.minerva_core.catalog.tax.application.TaxService;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.Tax;
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
@RequestMapping(TaxController.BASE_PATH)
@Validated
public class TaxController {

    public static final String BASE_PATH = "/api/v1/taxes";

    private final TaxService taxService;

    public TaxController(final TaxService taxService) {
        this.taxService = taxService;
    }

    @PostMapping
    public ResponseEntity<TaxResponse> create(@Valid @RequestBody final UpsertTaxRequest request) {
        final Tax tax = taxService.create(request.description(), request.rate(), request.surchargeRate());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(tax));
    }

    @GetMapping
    public List<TaxResponse> findAll() {
        return taxService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{taxId}")
    public TaxResponse getById(@PathVariable final UUID taxId) {
        return toResponse(taxService.getById(taxId));
    }

    @PutMapping("/{taxId}")
    public TaxResponse update(
            @PathVariable final UUID taxId,
            @Valid @RequestBody final UpsertTaxRequest request
    ) {
        final Tax tax = taxService.update(taxId, request.description(), request.rate(), request.surchargeRate());
        return toResponse(tax);
    }

    @DeleteMapping("/{taxId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID taxId) {
        taxService.delete(taxId);
        return ResponseEntity.noContent().build();
    }

    private TaxResponse toResponse(final Tax tax) {
        return new TaxResponse(tax.id(), tax.description(), tax.rate(), tax.surchargeRate());
    }

    public record UpsertTaxRequest(
            @NotBlank String description,
            @NotNull @DecimalMin("0.0") BigDecimal rate,
            @NotNull @DecimalMin("0.0") BigDecimal surchargeRate
    ) {
    }

    public record TaxResponse(
            UUID id,
            String description,
            BigDecimal rate,
            BigDecimal surchargeRate
    ) {
    }
}
