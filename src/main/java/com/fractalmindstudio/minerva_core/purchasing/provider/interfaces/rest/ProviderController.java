package com.fractalmindstudio.minerva_core.purchasing.provider.interfaces.rest;

import com.fractalmindstudio.minerva_core.purchasing.provider.application.ProviderService;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.Provider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ProviderController.BASE_PATH)
@Validated
public class ProviderController {

    public static final String BASE_PATH = "/api/v1/providers";

    private final ProviderService providerService;

    public ProviderController(final ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    public ResponseEntity<ProviderResponse> create(@Valid @RequestBody final UpsertProviderRequest request) {
        final Provider provider = providerService.create(
                request.businessName(), request.taxIdentifier(),
                request.address(), request.phone(), request.email(),
                Boolean.TRUE.equals(request.appliesSurcharge())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(provider));
    }

    @GetMapping
    public List<ProviderResponse> findAll() {
        return providerService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{providerId}")
    public ProviderResponse getById(@PathVariable final UUID providerId) {
        return toResponse(providerService.getById(providerId));
    }

    @PutMapping("/{providerId}")
    public ProviderResponse update(
            @PathVariable final UUID providerId,
            @Valid @RequestBody final UpsertProviderRequest request
    ) {
        final Provider provider = providerService.update(
                providerId, request.businessName(), request.taxIdentifier(),
                request.address(), request.phone(), request.email(),
                Boolean.TRUE.equals(request.appliesSurcharge())
        );
        return toResponse(provider);
    }

    @DeleteMapping("/{providerId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID providerId) {
        providerService.delete(providerId);
        return ResponseEntity.noContent().build();
    }

    private ProviderResponse toResponse(final Provider provider) {
        return new ProviderResponse(
                provider.id(), provider.businessName(), provider.taxIdentifier(),
                provider.address(), provider.phone(), provider.email(),
                provider.appliesSurcharge()
        );
    }

    public record UpsertProviderRequest(
            @NotBlank String businessName,
            @NotBlank String taxIdentifier,
            String address,
            String phone,
            String email,
            Boolean appliesSurcharge
    ) {
    }

    public record ProviderResponse(
            UUID id,
            String businessName,
            String taxIdentifier,
            String address,
            String phone,
            String email,
            boolean appliesSurcharge
    ) {
    }
}
