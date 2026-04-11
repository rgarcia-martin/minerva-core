package com.fractalmindstudio.minerva_core.sales.sale.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleLineTest {

    private static final BigDecimal VALID_UNIT_PRICE = new BigDecimal("15.00");
    private static final int VALID_QUANTITY = 2;

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

    @Test
    void shouldCalculateLineTotalForItemLine() {
        final SaleLine line = SaleLine.createForItem(
                UUID.randomUUID(), new BigDecimal("25.50"), UUID.randomUUID()
        );

        assertEquals(new BigDecimal("25.50"), line.lineTotal());
    }

    @Test
    void shouldCalculateLineTotalForFreeConceptLine() {
        final SaleLine line = SaleLine.createForFreeConcept(
                UUID.randomUUID(), 3, new BigDecimal("0.10"), UUID.randomUUID()
        );

        assertEquals(new BigDecimal("0.30"), line.lineTotal());
    }

    @Test
    void shouldScaleUnitPriceToTwoDecimalPlaces() {
        final SaleLine line = SaleLine.createForItem(
                UUID.randomUUID(), new BigDecimal("15.125"), UUID.randomUUID()
        );

        assertEquals(new BigDecimal("15.13"), line.unitPrice());
    }

    @Test
    void shouldRejectNullItemId() {
        assertThrows(NullPointerException.class, () -> SaleLine.createForItem(
                null, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }

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

    @Test
    void shouldRejectManualConstructionWithoutReference() {
        assertThrows(IllegalArgumentException.class, () -> new SaleLine(
                UUID.randomUUID(), null, null, 1, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectManualConstructionWithBothReferences() {
        assertThrows(IllegalArgumentException.class, () -> new SaleLine(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 1, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectItemLineWithQuantityDifferentFromOne() {
        assertThrows(IllegalArgumentException.class, () -> new SaleLine(
                UUID.randomUUID(), UUID.randomUUID(), null, 2, VALID_UNIT_PRICE, UUID.randomUUID()
        ));
    }
}
