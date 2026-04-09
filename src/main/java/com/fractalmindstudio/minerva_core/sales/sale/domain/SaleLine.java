package com.fractalmindstudio.minerva_core.sales.sale.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleLine(
        UUID id,
        UUID itemId,
        UUID freeConceptId,
        int quantity,
        BigDecimal unitPrice,
        UUID taxId
) {

    public static final String FIELD_ID = "saleLine.id";
    public static final String FIELD_ITEM_ID = "saleLine.itemId";
    public static final String FIELD_FREE_CONCEPT_ID = "saleLine.freeConceptId";
    public static final String FIELD_QUANTITY = "saleLine.quantity";
    public static final String FIELD_UNIT_PRICE = "saleLine.unitPrice";
    public static final String FIELD_TAX_ID = "saleLine.taxId";

    public SaleLine {
        DomainRules.requireNonNull(id, FIELD_ID);
        DomainRules.requirePositiveOrZero(unitPrice, FIELD_UNIT_PRICE);
        unitPrice = DomainRules.scaleMoney(unitPrice);
        DomainRules.requireNonNull(taxId, FIELD_TAX_ID);
    }

    public static SaleLine createForItem(
            final UUID itemId,
            final BigDecimal unitPrice,
            final UUID taxId
    ) {
        DomainRules.requireNonNull(itemId, FIELD_ITEM_ID);
        return new SaleLine(UUID.randomUUID(), itemId, null, 1, unitPrice, taxId);
    }

    public static SaleLine createForFreeConcept(
            final UUID freeConceptId,
            final int quantity,
            final BigDecimal unitPrice,
            final UUID taxId
    ) {
        DomainRules.requireNonNull(freeConceptId, FIELD_FREE_CONCEPT_ID);
        if (quantity <= 0) {
            throw new IllegalArgumentException(FIELD_QUANTITY + " must be greater than zero");
        }
        return new SaleLine(UUID.randomUUID(), null, freeConceptId, quantity, unitPrice, taxId);
    }

    public BigDecimal lineTotal() {
        return DomainRules.scaleMoney(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
