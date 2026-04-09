package com.fractalmindstudio.minerva_core.sales.client.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the Client domain model.
 * A client represents a customer who can optionally be associated with a sale
 * for tracking purposes, ticket generation, and future invoicing.
 */
class ClientTest {

    private static final String VALID_NAME = "Maria";
    private static final String VALID_LAST_NAME = "Garcia Lopez";
    private static final String VALID_EMAIL = "maria.garcia@email.com";
    private static final String VALID_PHONE = "+34 600 123 456";
    private static final String VALID_TAX_IDENTIFIER = "12345678Z";

    // --- Factory method ---

    @Test
    void shouldCreateClientWithGeneratedId() {
        final Client client = Client.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        );

        assertNotNull(client.id());
        assertEquals(VALID_NAME, client.name());
        assertEquals(VALID_LAST_NAME, client.lastName());
        assertEquals(VALID_EMAIL, client.email());
        assertEquals(VALID_PHONE, client.phone());
        assertEquals(VALID_TAX_IDENTIFIER, client.taxIdentifier());
    }

    // --- Optional fields ---

    @Test
    void shouldAllowNullEmail() {
        final Client client = Client.create(
                VALID_NAME, VALID_LAST_NAME, null,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        );

        assertNull(client.email());
    }

    @Test
    void shouldAllowNullPhone() {
        final Client client = Client.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                null, VALID_TAX_IDENTIFIER
        );

        assertNull(client.phone());
    }

    @Test
    void shouldAllowNullTaxIdentifier() {
        final Client client = Client.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PHONE, null
        );

        assertNull(client.taxIdentifier());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new Client(
                null, VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        ));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Client.create(
                "", VALID_LAST_NAME, VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        ));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> Client.create(
                null, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        ));
    }

    @Test
    void shouldRejectBlankLastName() {
        assertThrows(IllegalArgumentException.class, () -> Client.create(
                VALID_NAME, "", VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        ));
    }

    @Test
    void shouldTrimName() {
        final Client client = Client.create(
                "  Maria  ", VALID_LAST_NAME, VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        );

        assertEquals("Maria", client.name());
    }

    @Test
    void shouldTrimLastName() {
        final Client client = Client.create(
                VALID_NAME, "  Garcia Lopez  ", VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        );

        assertEquals("Garcia Lopez", client.lastName());
    }

    // --- Reconstruction from persistence ---

    @Test
    void shouldReconstructClientFromPersistence() {
        final UUID existingId = UUID.randomUUID();

        final Client client = new Client(
                existingId, VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PHONE, VALID_TAX_IDENTIFIER
        );

        assertEquals(existingId, client.id());
    }
}
