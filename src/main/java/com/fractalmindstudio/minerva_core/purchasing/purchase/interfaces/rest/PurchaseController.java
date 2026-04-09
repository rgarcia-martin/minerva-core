package com.fractalmindstudio.minerva_core.purchasing.purchase.interfaces.rest;

import com.fractalmindstudio.minerva_core.purchasing.purchase.application.PurchaseService;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(PurchaseController.BASE_PATH)
@Validated
public class PurchaseController {

    public static final String BASE_PATH = "/api/v1/purchases";

    private final PurchaseService purchaseService;

    public PurchaseController(final PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody final UpsertPurchaseRequest request) {
        final Purchase purchase = purchaseService.create(
                request.createdOn(),
                request.finishDate(),
                request.state(),
                request.code(),
                request.providerCode(),
                request.providerId(),
                request.locationId(),
                Boolean.TRUE.equals(request.deposit()),
                toPurchaseLines(request.lines())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(purchase));
    }

    @GetMapping
    public List<PurchaseResponse> findAll() {
        return purchaseService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{purchaseId}")
    public PurchaseResponse getById(@PathVariable final UUID purchaseId) {
        return toResponse(purchaseService.getById(purchaseId));
    }

    @PutMapping("/{purchaseId}")
    public PurchaseResponse update(
            @PathVariable final UUID purchaseId,
            @Valid @RequestBody final UpsertPurchaseRequest request
    ) {
        final Purchase purchase = purchaseService.update(
                purchaseId,
                request.createdOn(),
                request.finishDate(),
                request.state(),
                request.code(),
                request.providerCode(),
                request.providerId(),
                request.locationId(),
                Boolean.TRUE.equals(request.deposit()),
                toPurchaseLines(request.lines())
        );
        return toResponse(purchase);
    }

    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID purchaseId) {
        purchaseService.delete(purchaseId);
        return ResponseEntity.noContent().build();
    }

    private List<PurchaseLine> toPurchaseLines(final List<PurchaseLineRequest> lines) {
        if (lines == null) {
            return List.of();
        }

        return lines.stream()
                .map(line -> PurchaseLine.create(
                        line.articleId(), line.quantity(), line.buyPrice(),
                        line.profitMargin(), line.taxId()
                ))
                .toList();
    }

    private PurchaseResponse toResponse(final Purchase purchase) {
        return new PurchaseResponse(
                purchase.id(),
                purchase.createdOn(),
                purchase.finishDate(),
                purchase.state(),
                purchase.code(),
                purchase.providerCode(),
                purchase.providerId(),
                purchase.locationId(),
                purchase.deposit(),
                purchase.lines().stream().map(this::toResponse).toList(),
                purchase.totalCost()
        );
    }

    private PurchaseLineResponse toResponse(final PurchaseLine purchaseLine) {
        return new PurchaseLineResponse(
                purchaseLine.id(),
                purchaseLine.articleId(),
                purchaseLine.quantity(),
                purchaseLine.buyPrice(),
                purchaseLine.profitMargin(),
                purchaseLine.taxId()
        );
    }

    public record UpsertPurchaseRequest(
            LocalDateTime createdOn,
            LocalDateTime finishDate,
            PurchaseState state,
            @NotBlank String code,
            @NotBlank String providerCode,
            @NotNull UUID providerId,
            @NotNull UUID locationId,
            Boolean deposit,
            @Valid List<PurchaseLineRequest> lines
    ) {
    }

    public record PurchaseLineRequest(
            @NotNull UUID articleId,
            @Min(1) int quantity,
            @NotNull @DecimalMin("0.0") BigDecimal buyPrice,
            @NotNull @DecimalMin("0.0") BigDecimal profitMargin,
            @NotNull UUID taxId
    ) {
    }

    public record PurchaseResponse(
            UUID id,
            LocalDateTime createdOn,
            LocalDateTime finishDate,
            PurchaseState state,
            String code,
            String providerCode,
            UUID providerId,
            UUID locationId,
            boolean deposit,
            List<PurchaseLineResponse> lines,
            BigDecimal totalCost
    ) {
    }

    public record PurchaseLineResponse(
            UUID id,
            UUID articleId,
            int quantity,
            BigDecimal buyPrice,
            BigDecimal profitMargin,
            UUID taxId
    ) {
    }
}
