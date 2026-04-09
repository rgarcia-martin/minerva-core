package com.fractalmindstudio.minerva_core.catalog.tax.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the Tax domain model.
 * Tax represents a fiscal rate (IVA) paired with its equivalence surcharge
 * following the Spanish tax system (recargo de equivalencia).
 */
class TaxTest {

    private static final String GENERAL_VAT_DESCRIPTION = "IVA General";
    private static final BigDecimal GENERAL_VAT_RATE = new BigDecimal("21.0000");
    private static final BigDecimal GENERAL_SURCHARGE_RATE = new BigDecimal("5.2000");
    private static final BigDecimal REDUCED_VAT_RATE = new BigDecimal("10.0000");
    private static final BigDecimal REDUCED_SURCHARGE_RATE = new BigDecimal("1.4000");
    private static final BigDecimal SUPER_REDUCED_VAT_RATE = new BigDecimal("4.0000");
    private static final BigDecimal SUPER_REDUCED_SURCHARGE_RATE = new BigDecimal("0.5000");

    // --- Factory method ---

    @Test
    void shouldCreateTaxWithGeneratedId() {
        final Tax tax = Tax.create(GENERAL_VAT_DESCRIPTION, GENERAL_VAT_RATE, GENERAL_SURCHARGE_RATE);

        assertNotNull(tax.id());
        assertEquals(GENERAL_VAT_DESCRIPTION, tax.description());
    }

    // --- Rate scaling ---

    @Test
    void shouldScaleRateToFourDecimalPlaces() {
        final Tax tax = Tax.create("VAT", new BigDecimal("21.45678"), BigDecimal.ZERO);

        assertEquals(new BigDecimal("21.4568"), tax.rate());
    }

    @Test
    void shouldScaleSurchargeRateToFourDecimalPlaces() {
        final Tax tax = Tax.create("VAT", GENERAL_VAT_RATE, new BigDecimal("5.23456"));

        assertEquals(new BigDecimal("5.2346"), tax.surchargeRate());
    }

    // --- Spanish tax system validation ---

    @Test
    void shouldCreateGeneralVatWithSurcharge() {
        final Tax tax = Tax.create(GENERAL_VAT_DESCRIPTION, GENERAL_VAT_RATE, GENERAL_SURCHARGE_RATE);

        assertEquals(GENERAL_VAT_RATE, tax.rate());
        assertEquals(GENERAL_SURCHARGE_RATE, tax.surchargeRate());
    }

    @Test
    void shouldCreateReducedVatWithSurcharge() {
        final Tax tax = Tax.create("IVA Reducido", REDUCED_VAT_RATE, REDUCED_SURCHARGE_RATE);

        assertEquals(REDUCED_VAT_RATE, tax.rate());
        assertEquals(REDUCED_SURCHARGE_RATE, tax.surchargeRate());
    }

    @Test
    void shouldCreateSuperReducedVatWithSurcharge() {
        final Tax tax = Tax.create("IVA Superreducido", SUPER_REDUCED_VAT_RATE, SUPER_REDUCED_SURCHARGE_RATE);

        assertEquals(SUPER_REDUCED_VAT_RATE, tax.rate());
        assertEquals(SUPER_REDUCED_SURCHARGE_RATE, tax.surchargeRate());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(
                NullPointerException.class,
                () -> new Tax(null, GENERAL_VAT_DESCRIPTION, GENERAL_VAT_RATE, GENERAL_SURCHARGE_RATE)
        );
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Tax.create("", GENERAL_VAT_RATE, GENERAL_SURCHARGE_RATE)
        );
    }

    @Test
    void shouldRejectNegativeRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Tax.create("VAT", new BigDecimal("-0.01"), GENERAL_SURCHARGE_RATE)
        );
    }

    @Test
    void shouldRejectNegativeSurchargeRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Tax.create("VAT", GENERAL_VAT_RATE, new BigDecimal("-0.01"))
        );
    }

    @Test
    void shouldRejectNullRate() {
        assertThrows(
                NullPointerException.class,
                () -> Tax.create("VAT", null, GENERAL_SURCHARGE_RATE)
        );
    }

    @Test
    void shouldRejectNullSurchargeRate() {
        assertThrows(
                NullPointerException.class,
                () -> Tax.create("VAT", GENERAL_VAT_RATE, null)
        );
    }

    // --- Reconstruction from persistence ---

    @Test
    void shouldReconstructTaxFromPersistence() {
        final UUID existingId = UUID.randomUUID();

        final Tax tax = new Tax(existingId, GENERAL_VAT_DESCRIPTION, GENERAL_VAT_RATE, GENERAL_SURCHARGE_RATE);

        assertEquals(existingId, tax.id());
        assertEquals(GENERAL_VAT_DESCRIPTION, tax.description());
        assertEquals(GENERAL_VAT_RATE, tax.rate());
        assertEquals(GENERAL_SURCHARGE_RATE, tax.surchargeRate());
    }
}
