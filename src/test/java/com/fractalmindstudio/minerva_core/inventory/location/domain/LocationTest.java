package com.fractalmindstudio.minerva_core.inventory.location.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the Location domain model.
 * A location represents a physical place where items are stored
 * (e.g., warehouse, shop, shelf).
 */
class LocationTest {

    private static final String VALID_NAME = "Main Warehouse";
    private static final String VALID_DESCRIPTION = "Central storage facility";

    // --- Factory method ---

    @Test
    void shouldCreateLocationWithGeneratedId() {
        final Location location = Location.create(VALID_NAME, VALID_DESCRIPTION);

        assertNotNull(location.id());
        assertEquals(VALID_NAME, location.name());
        assertEquals(VALID_DESCRIPTION, location.description());
    }

    @Test
    void shouldAllowNullDescription() {
        final Location location = Location.create(VALID_NAME, null);

        assertNull(location.description());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new Location(null, VALID_NAME, VALID_DESCRIPTION));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Location.create("", VALID_DESCRIPTION));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> Location.create(null, VALID_DESCRIPTION));
    }

    @Test
    void shouldTrimName() {
        final Location location = Location.create("  Main Warehouse  ", VALID_DESCRIPTION);

        assertEquals(VALID_NAME, location.name());
    }

    // --- Reconstruction from persistence ---

    @Test
    void shouldReconstructLocationFromPersistence() {
        final UUID existingId = UUID.randomUUID();

        final Location location = new Location(existingId, VALID_NAME, VALID_DESCRIPTION);

        assertEquals(existingId, location.id());
    }
}
