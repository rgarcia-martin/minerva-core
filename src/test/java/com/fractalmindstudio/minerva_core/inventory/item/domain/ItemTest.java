package com.fractalmindstudio.minerva_core.inventory.item.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Item domain model.
 * An item represents a single physical unit of stock generated from a
 * purchase delivery note. It tracks its origin (provider, purchase line),
 * its location, applicable taxes, and its lifecycle status.
 */
class ItemTest {

    private static final BigDecimal VALID_COST = new BigDecimal("99.99");

    // --- Factory method ---

    @Test
    void shouldCreateItemWithGeneratedId() {
        final UUID articleId = UUID.randomUUID();
        final UUID providerId = UUID.randomUUID();
        final UUID locationId = UUID.randomUUID();
        final UUID taxId = UUID.randomUUID();

        final Item item = Item.create(
                articleId, null, null, false, VALID_COST,
                taxId, null, providerId, locationId
        );

        assertNotNull(item.id());
        assertEquals(articleId, item.articleId());
        assertEquals(providerId, item.providerId());
        assertEquals(locationId, item.locationId());
        assertEquals(taxId, item.buyTaxId());
    }

    // --- Default status ---

    @Test
    void shouldDefaultStatusToAvailableWhenNull() {
        final Item item = Item.create(
                UUID.randomUUID(), null, null, false, VALID_COST,
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        assertEquals(ItemStatus.AVAILABLE, item.itemStatus());
    }

    @Test
    void shouldPreserveExplicitStatus() {
        final Item item = Item.create(
                UUID.randomUUID(), ItemStatus.OPENED, null, true, VALID_COST,
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        assertEquals(ItemStatus.OPENED, item.itemStatus());
    }

    // --- Cost scaling ---

    @Test
    void shouldScaleCostToTwoDecimalPlaces() {
        final Item item = Item.create(
                UUID.randomUUID(), null, null, false, new BigDecimal("99.999"),
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        assertEquals(new BigDecimal("100.00"), item.cost());
    }

    // --- Parent-child (package) relationship ---

    @Test
    void shouldCreateParentItemWithOpenedStatus() {
        final Item parent = Item.create(
                UUID.randomUUID(), ItemStatus.OPENED, null, true, new BigDecimal("12.00"),
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        assertEquals(ItemStatus.OPENED, parent.itemStatus());
        assertTrue(parent.hasChildren());
    }

    @Test
    void shouldCreateChildItemWithParentReference() {
        final UUID parentItemId = UUID.randomUUID();

        final Item child = Item.create(
                UUID.randomUUID(), ItemStatus.AVAILABLE, parentItemId, false, new BigDecimal("1.00"),
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        assertEquals(parentItemId, child.parentItemId());
        assertEquals(ItemStatus.AVAILABLE, child.itemStatus());
        assertFalse(child.hasChildren());
    }

    // --- Status transitions ---

    @Test
    void shouldMarkItemAsSold() {
        final Item item = Item.create(
                UUID.randomUUID(), ItemStatus.AVAILABLE, null, false, VALID_COST,
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        final Item sold = item.markAsSold();

        assertEquals(ItemStatus.SOLD, sold.itemStatus());
        assertEquals(item.id(), sold.id());
    }

    @Test
    void shouldReturnItemToAvailable() {
        final Item soldItem = Item.create(
                UUID.randomUUID(), ItemStatus.SOLD, null, false, VALID_COST,
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        final Item returned = soldItem.markAsAvailable();

        assertEquals(ItemStatus.AVAILABLE, returned.itemStatus());
    }

    // --- Provider origin tracking ---

    @Test
    void shouldTrackProviderOrigin() {
        final UUID providerId = UUID.randomUUID();

        final Item item = Item.create(
                UUID.randomUUID(), null, null, false, VALID_COST,
                UUID.randomUUID(), null, providerId, UUID.randomUUID()
        );

        assertEquals(providerId, item.providerId());
    }

    @Test
    void shouldTrackLocationAssignment() {
        final UUID locationId = UUID.randomUUID();

        final Item item = Item.create(
                UUID.randomUUID(), null, null, false, VALID_COST,
                UUID.randomUUID(), null, UUID.randomUUID(), locationId
        );

        assertEquals(locationId, item.locationId());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullArticleId() {
        assertThrows(NullPointerException.class, () -> Item.create(
                null, null, null, false, VALID_COST,
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNegativeCost() {
        assertThrows(IllegalArgumentException.class, () -> Item.create(
                UUID.randomUUID(), null, null, false, new BigDecimal("-0.01"),
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNullCost() {
        assertThrows(NullPointerException.class, () -> Item.create(
                UUID.randomUUID(), null, null, false, null,
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        ));
    }

    @Test
    void shouldAllowNullOptionalTax() {
        final Item item = Item.create(
                UUID.randomUUID(), null, null, false, VALID_COST,
                null, null, UUID.randomUUID(), UUID.randomUUID()
        );

        assertNull(item.buyTaxId());
    }
}
