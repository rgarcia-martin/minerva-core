package com.fractalmindstudio.minerva_core.payment.paymentmethod.interfaces.rest;

import com.fractalmindstudio.minerva_core.payment.paymentmethod.application.PaymentMethodService;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethod;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodType;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(PaymentMethodController.BASE_PATH)
@Validated
public class PaymentMethodController {

    public static final String BASE_PATH = "/api/v1/payment-methods";

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(final PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> create(@Valid @RequestBody final UpsertPaymentMethodRequest request) {
        final PaymentMethod paymentMethod = paymentMethodService.create(
                request.name(),
                request.type(),
                request.configuration()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(paymentMethod));
    }

    @GetMapping
    public List<PaymentMethodResponse> findAll() {
        return paymentMethodService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{paymentMethodId}")
    public PaymentMethodResponse getById(@PathVariable final UUID paymentMethodId) {
        return toResponse(paymentMethodService.getById(paymentMethodId));
    }

    @PutMapping("/{paymentMethodId}")
    public PaymentMethodResponse update(
            @PathVariable final UUID paymentMethodId,
            @Valid @RequestBody final UpsertPaymentMethodRequest request
    ) {
        return toResponse(paymentMethodService.update(
                paymentMethodId,
                request.name(),
                request.type(),
                request.configuration()
        ));
    }

    @DeleteMapping("/{paymentMethodId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID paymentMethodId) {
        paymentMethodService.delete(paymentMethodId);
        return ResponseEntity.noContent().build();
    }

    private PaymentMethodResponse toResponse(final PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.id(),
                paymentMethod.name(),
                paymentMethod.type(),
                paymentMethod.configuration()
        );
    }

    public record UpsertPaymentMethodRequest(
            @NotBlank String name,
            @NotNull PaymentMethodType type,
            String configuration
    ) {
    }

    public record PaymentMethodResponse(
            UUID id,
            String name,
            PaymentMethodType type,
            String configuration
    ) {
    }
}
