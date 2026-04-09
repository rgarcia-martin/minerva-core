package com.fractalmindstudio.minerva_core.sales.sale.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the SaleLine domain model.
 * A sale line can reference either an inventory item (via itemId) or a free
 * concept (via freeConceptId), but not both. This distinction allows a single
 * sale to mix physical stock and services (Strategy pattern for line types).
 */
class SaleLineTest {

    private static final BigDecimal VALID_UNIT_PRICE = new BigDecimal("15.00");
    private static final int VALID_QUANTITY = 2;

    // --- Item-based line ---

    @Test
    void shouldCreateItemLineWithGeneratedId() {
        final UUID itemId = UUID.randomUUID();
        final UUID taxId = UUID.randomUUID();

        final SaleLine line = SaleLine.createForItem(itemId, VALID_UNIT_PRICE, taxId);

        assertNotNull(line.id());
        assertEquals(itemId, line.itemId());
        assertNull(line.freeConceptId());
        assertEquals(1, line.quantity());
        assertEquals(VALID_UNIT_PRICE, line.unitPrice());
        assertEquals(taxId, line.taxId());
    }

    @Test
    void shouldAlwaysSetQuantityToOneForItemLines() {
        final SaleLine line = SaleLine.createForItem(
                UUID.randomUUID(), VALID_UNIT_PRICE, UUID.randomUUID()
        );

        assertEquals(1, line.quantity());
    }

    // --- Free concept-based line ---

    @Test
    void shouldCreateFreeConceptLineWithQuantity() {
        final UUID freeConceptId = UUID.randomUUID();
        final UUID taxId = UUID.randomUUID();

        final SaleLine line = SaleLine.createForFreeConcept(
                freeConceptId, VALID_QUANTITY, VALID_UNIT_PRICE, taxId
        );

        assertNotNull(line.id());
        assertNull(line.itemId());
        assertEquals(freeConceptId, line.freeConceptId());
        assertEquals(VALID_QUANTITY, line.quantity());
    }

    // --- Line total calculation ---

    @Test
    void shouldCalculateLineTotalForItemLine() {
        final SaleLine line = SaleLine.createForItem(
                UUID.randomUUID(), new BigDecimal("25.50"), UUID.randomUUID()
        );

        // 1 * 25.50 = 25.50
        assertEquals(new BigDecimal("25.50"), line.lineTotal());
    }

    @Test
    void shouldCalculateLineTotalForFreeConceptLine() {
        final SaleLine line = SaleLine.createForFreeConcept(
                UUID.randomUUID(), 3, new BigDecimal("0.10"), UUID.randomUUID()
        );

        // 3 * 0.10 = 0.30
        assertEquals(new BigDecimal("0.30"), line.lineTotal());
    }

    // --- Price scaling ---

    @Test
    void shouldScaleUnitPriceToTwoDecimalPlaces() {
        final SaleLine line = SaleLine.createForItem(
                UUID.randomUUID(), new BigDecimal("15.125"), UUID.randomUUID()
        );

        assertEquals(new BigDecimal("15.13"), line.unitPrice());
    }

    // --- Invariant enforcement: item lines ---

    @Test
    void shouldRejectNullItemId() {
        assertThrows(NullPointerException.class, () -> SaleLine.createForItem(
                null, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }

    // --- Invariant enforcement: free concept lines ---

    @Test
    void shouldRejectNullFreeConceptId() {
        assertThrows(NullPointerException.class, () -> SaleLine.createForFreeConcept(
                null, VALID_QUANTITY, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectZeroQuantityForFreeConcept() {
        assertThrows(IllegalArgumentException.class, () -> SaleLine.createForFreeConcept(
                UUID.randomUUID(), 0, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNegativeQuantityForFreeConcept() {
        assertThrows(IllegalArgumentException.class, () -> SaleLine.createForFreeConcept(
                UUID.randomUUID(), -1, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }

    // --- Invariant enforcement: shared ---

    @Test
    void shouldRejectNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class, () -> SaleLine.createForItem(
                UUID.randomUUID(), new BigDecimal("-0.01"), UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNullUnitPrice() {
        assertThrows(NullPointerException.class, () -> SaleLine.createForItem(
                UUID.randomUUID(), null, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNullTaxId() {
        assertThrows(NullPointerException.class, () -> SaleLine.createForItem(
                UUID.randomUUID(), VALID_UNIT_PRICE, null
        ));
    }
}
