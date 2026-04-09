package com.fractalmindstudio.minerva_core.sales.sale.interfaces.rest;

import com.fractalmindstudio.minerva_core.sales.sale.application.SaleService;
import com.fractalmindstudio.minerva_core.sales.sale.domain.Sale;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleLine;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleState;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(SaleController.BASE_PATH)
@Validated
public class SaleController {

    public static final String BASE_PATH = "/api/v1/sales";

    private final SaleService saleService;

    public SaleController(final SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody final CreateSaleRequest request) {
        final Sale sale = saleService.create(
                request.code(), request.employeeId(), request.clientId(),
                request.paymentMethodId(), toSaleLines(request.lines())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(sale));
    }

    @GetMapping
    public List<SaleResponse> findAll() {
        return saleService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{saleId}")
    public SaleResponse getById(@PathVariable final UUID saleId) {
        return toResponse(saleService.getById(saleId));
    }

    @DeleteMapping("/{saleId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID saleId) {
        saleService.delete(saleId);
        return ResponseEntity.noContent().build();
    }

    private List<SaleLine> toSaleLines(final List<SaleLineRequest> lines) {
        if (lines == null) {
            return List.of();
        }

        return lines.stream()
                .map(line -> {
                    if (line.itemId() != null) {
                        return SaleLine.createForItem(line.itemId(), line.unitPrice(), line.taxId());
                    }
                    return SaleLine.createForFreeConcept(
                            line.freeConceptId(),
                            line.quantity() == null ? 1 : line.quantity(),
                            line.unitPrice(), line.taxId()
                    );
                })
                .toList();
    }

    private SaleResponse toResponse(final Sale sale) {
        return new SaleResponse(
                sale.id(), sale.code(), sale.employeeId(), sale.clientId(),
                sale.paymentMethodId(), sale.state(), sale.createdOn(),
                sale.lines().stream().map(this::toResponse).toList(),
                sale.totalAmount()
        );
    }

    private SaleLineResponse toResponse(final SaleLine saleLine) {
        return new SaleLineResponse(
                saleLine.id(), saleLine.itemId(), saleLine.freeConceptId(),
                saleLine.quantity(), saleLine.unitPrice(), saleLine.taxId()
        );
    }

    public record CreateSaleRequest(
            @NotBlank String code,
            @NotNull UUID employeeId,
            UUID clientId,
            @NotNull UUID paymentMethodId,
            @Valid List<SaleLineRequest> lines
    ) {
    }

    public record SaleLineRequest(
            UUID itemId,
            UUID freeConceptId,
            Integer quantity,
            @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
            @NotNull UUID taxId
    ) {
    }

    public record SaleResponse(
            UUID id,
            String code,
            UUID employeeId,
            UUID clientId,
            UUID paymentMethodId,
            SaleState state,
            LocalDateTime createdOn,
            List<SaleLineResponse> lines,
            BigDecimal totalAmount
    ) {
    }

    public record SaleLineResponse(
            UUID id,
            UUID itemId,
            UUID freeConceptId,
            int quantity,
            BigDecimal unitPrice,
            UUID taxId
    ) {
    }
}
