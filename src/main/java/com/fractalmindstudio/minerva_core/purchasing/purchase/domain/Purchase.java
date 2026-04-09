package com.fractalmindstudio.minerva_core.purchasing.purchase.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Purchase(
        UUID id,
        LocalDateTime createdOn,
        LocalDateTime finishDate,
        PurchaseState state,
        String code,
        String providerCode,
        UUID providerId,
        UUID locationId,
        boolean deposit,
        List<PurchaseLine> lines,
        BigDecimal totalCost
) {

    public static final String FIELD_ID = "purchase.id";
    public static final String FIELD_CREATED_ON = "purchase.createdOn";
    public static final String FIELD_CODE = "purchase.code";
    public static final String FIELD_PROVIDER_CODE = "purchase.providerCode";
    public static final String FIELD_PROVIDER_ID = "purchase.providerId";
    public static final String FIELD_LOCATION_ID = "purchase.locationId";
    public static final String FIELD_LINES = "purchase.lines";
    public static final String FIELD_TOTAL_COST = "purchase.totalCost";

    public Purchase {
        DomainRules.requireNonNull(id, FIELD_ID);
        DomainRules.requireNonNull(createdOn, FIELD_CREATED_ON);
        state = state == null ? PurchaseState.NEW : state;
        code = DomainRules.requireNonBlank(code, FIELD_CODE);
        providerCode = DomainRules.requireNonBlank(providerCode, FIELD_PROVIDER_CODE);
        DomainRules.requireNonNull(providerId, FIELD_PROVIDER_ID);
        DomainRules.requireNonNull(locationId, FIELD_LOCATION_ID);
        DomainRules.requireNonNull(lines, FIELD_LINES);
        lines = List.copyOf(lines);
        DomainRules.requirePositiveOrZero(totalCost, FIELD_TOTAL_COST);
        totalCost = DomainRules.scaleMoney(totalCost);
    }

    public static Purchase create(
            final LocalDateTime createdOn,
            final LocalDateTime finishDate,
            final PurchaseState state,
            final String code,
            final String providerCode,
            final UUID providerId,
            final UUID locationId,
            final boolean deposit,
            final List<PurchaseLine> lines
    ) {
        DomainRules.requireNonNull(lines, FIELD_LINES);
        return new Purchase(
                UUID.randomUUID(),
                createdOn == null ? LocalDateTime.now() : createdOn,
                finishDate,
                state == null ? PurchaseState.NEW : state,
                code,
                providerCode,
                providerId,
                locationId,
                deposit,
                lines,
                BigDecimal.ZERO
        ).recalculateTotalCost();
    }

    public Purchase recalculateTotalCost() {
        final BigDecimal calculatedTotal = lines.stream()
                .map(PurchaseLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Purchase(id, createdOn, finishDate, state, code, providerCode, providerId, locationId, deposit, lines, calculatedTotal);
    }

    public Purchase markAsPaid() {
        return new Purchase(id, createdOn, finishDate, PurchaseState.PAID, code, providerCode, providerId, locationId, deposit, lines, totalCost);
    }
}
