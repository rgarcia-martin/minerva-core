package com.fractalmindstudio.minerva_core.catalog.freeconcept.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the FreeConcept domain model.
 * A free concept represents a service or product that is sold without
 * being linked to physical inventory (e.g., plastic bags, photocopies,
 * table service). Each concept has a barcode for scanning at point of sale.
 */
class FreeConceptTest {

    private static final String VALID_NAME = "B&W Photocopy";
    private static final String VALID_BARCODE = "8400000050001";
    private static final BigDecimal VALID_PRICE = new BigDecimal("0.10");
    private static final String VALID_DESCRIPTION = "Single-page black and white photocopy";

    // --- Factory method ---

    @Test
    void shouldCreateFreeConceptWithGeneratedId() {
        final UUID taxId = UUID.randomUUID();

        final FreeConcept concept = FreeConcept.create(
                VALID_NAME, VALID_BARCODE, VALID_PRICE, taxId, VALID_DESCRIPTION
        );

        assertNotNull(concept.id());
        assertEquals(VALID_NAME, concept.name());
        assertEquals(VALID_BARCODE, concept.barcode());
        assertEquals(VALID_PRICE, concept.price());
        assertEquals(taxId, concept.taxId());
        assertEquals(VALID_DESCRIPTION, concept.description());
    }

    // --- Price scaling ---

    @Test
    void shouldScalePriceToTwoDecimalPlaces() {
        final FreeConcept concept = FreeConcept.create(
                VALID_NAME, VALID_BARCODE, new BigDecimal("0.105"),
                UUID.randomUUID(), null
        );

        assertEquals(new BigDecimal("0.11"), concept.price());
    }

    // --- Optional fields ---

    @Test
    void shouldAllowNullDescription() {
        final FreeConcept concept = FreeConcept.create(
                VALID_NAME, VALID_BARCODE, VALID_PRICE,
                UUID.randomUUID(), null
        );

        assertNull(concept.description());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new FreeConcept(
                null, VALID_NAME, VALID_BARCODE, VALID_PRICE,
                UUID.randomUUID(), VALID_DESCRIPTION
        ));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> FreeConcept.create(
                "", VALID_BARCODE, VALID_PRICE,
                UUID.randomUUID(), null
        ));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> FreeConcept.create(
                null, VALID_BARCODE, VALID_PRICE,
                UUID.randomUUID(), null
        ));
    }

    @Test
    void shouldRejectBlankBarcode() {
        assertThrows(IllegalArgumentException.class, () -> FreeConcept.create(
                VALID_NAME, "", VALID_PRICE,
                UUID.randomUUID(), null
        ));
    }

    @Test
    void shouldRejectNullBarcode() {
        assertThrows(IllegalArgumentException.class, () -> FreeConcept.create(
                VALID_NAME, null, VALID_PRICE,
                UUID.randomUUID(), null
        ));
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> FreeConcept.create(
                VALID_NAME, VALID_BARCODE, new BigDecimal("-0.01"),
                UUID.randomUUID(), null
        ));
    }

    @Test
    void shouldRejectNullPrice() {
        assertThrows(NullPointerException.class, () -> FreeConcept.create(
                VALID_NAME, VALID_BARCODE, null,
                UUID.randomUUID(), null
        ));
    }

    @Test
    void shouldRejectNullTaxId() {
        assertThrows(NullPointerException.class, () -> FreeConcept.create(
                VALID_NAME, VALID_BARCODE, VALID_PRICE,
                null, null
        ));
    }

    @Test
    void shouldTrimName() {
        final FreeConcept concept = FreeConcept.create(
                "  B&W Photocopy  ", VALID_BARCODE, VALID_PRICE,
                UUID.randomUUID(), null
        );

        assertEquals("B&W Photocopy", concept.name());
    }

    @Test
    void shouldTrimBarcode() {
        final FreeConcept concept = FreeConcept.create(
                VALID_NAME, "  8400000050001  ", VALID_PRICE,
                UUID.randomUUID(), null
        );

        assertEquals(VALID_BARCODE, concept.barcode());
    }

    // --- Reconstruction from persistence ---

    @Test
    void shouldReconstructFreeConceptFromPersistence() {
        final UUID existingId = UUID.randomUUID();
        final UUID taxId = UUID.randomUUID();

        final FreeConcept concept = new FreeConcept(
                existingId, VALID_NAME, VALID_BARCODE, VALID_PRICE,
                taxId, VALID_DESCRIPTION
        );

        assertEquals(existingId, concept.id());
        assertEquals(taxId, concept.taxId());
    }
}
