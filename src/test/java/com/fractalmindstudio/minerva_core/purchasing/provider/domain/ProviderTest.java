package com.fractalmindstudio.minerva_core.purchasing.provider.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Provider domain model.
 * A provider represents a supplier with business data and an equivalence
 * surcharge flag that determines whether the surcharge is applied when
 * purchasing from this provider.
 */
class ProviderTest {

    private static final String VALID_BUSINESS_NAME = "Office Supplies S.L.";
    private static final String VALID_TAX_IDENTIFIER = "B12345678";
    private static final String VALID_ADDRESS = "Industrial Avenue 15, Madrid";
    private static final String VALID_PHONE = "+34 912 345 678";
    private static final String VALID_EMAIL = "sales@officesupplies.es";

    // --- Factory method ---

    @Test
    void shouldCreateProviderWithGeneratedId() {
        final Provider provider = Provider.create(
                VALID_BUSINESS_NAME, VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        );

        assertNotNull(provider.id());
        assertEquals(VALID_BUSINESS_NAME, provider.businessName());
        assertEquals(VALID_TAX_IDENTIFIER, provider.taxIdentifier());
    }

    // --- Equivalence surcharge flag ---

    @Test
    void shouldCreateProviderWithSurchargeEnabled() {
        final Provider provider = Provider.create(
                VALID_BUSINESS_NAME, VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, true
        );

        assertTrue(provider.appliesSurcharge());
    }

    @Test
    void shouldCreateProviderWithSurchargeDisabled() {
        final Provider provider = Provider.create(
                VALID_BUSINESS_NAME, VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        );

        assertFalse(provider.appliesSurcharge());
    }

    // --- Optional fields ---

    @Test
    void shouldAllowNullOptionalFields() {
        final Provider provider = Provider.create(
                VALID_BUSINESS_NAME, VALID_TAX_IDENTIFIER,
                null, null, null, false
        );

        assertNull(provider.address());
        assertNull(provider.phone());
        assertNull(provider.email());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new Provider(
                null, VALID_BUSINESS_NAME, VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        ));
    }

    @Test
    void shouldRejectBlankBusinessName() {
        assertThrows(IllegalArgumentException.class, () -> Provider.create(
                "", VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        ));
    }

    @Test
    void shouldRejectNullBusinessName() {
        assertThrows(IllegalArgumentException.class, () -> Provider.create(
                null, VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        ));
    }

    @Test
    void shouldRejectBlankTaxIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> Provider.create(
                VALID_BUSINESS_NAME, "",
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        ));
    }

    @Test
    void shouldTrimBusinessName() {
        final Provider provider = Provider.create(
                "  Office Supplies S.L.  ", VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        );

        assertEquals(VALID_BUSINESS_NAME, provider.businessName());
    }

    @Test
    void shouldTrimTaxIdentifier() {
        final Provider provider = Provider.create(
                VALID_BUSINESS_NAME, "  B12345678  ",
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, false
        );

        assertEquals(VALID_TAX_IDENTIFIER, provider.taxIdentifier());
    }

    // --- Reconstruction from persistence ---

    @Test
    void shouldReconstructProviderFromPersistence() {
        final UUID existingId = UUID.randomUUID();

        final Provider provider = new Provider(
                existingId, VALID_BUSINESS_NAME, VALID_TAX_IDENTIFIER,
                VALID_ADDRESS, VALID_PHONE, VALID_EMAIL, true
        );

        assertEquals(existingId, provider.id());
        assertTrue(provider.appliesSurcharge());
    }
}
