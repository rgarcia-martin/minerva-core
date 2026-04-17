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

class ItemTest {

    private static final BigDecimal VALID_COST = new BigDecimal("99.99");

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

    @Test
    void shouldScaleCostToTwoDecimalPlaces() {
        final Item item = Item.create(
                UUID.randomUUID(), null, null, false, new BigDecimal("99.999"),
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()
        );

        assertEquals(new BigDecimal("100.00"), item.cost());
    }

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
    void shouldTrackOriginPurchase() {
        final UUID purchaseId = UUID.randomUUID();

        final Item item = Item.create(
                UUID.randomUUID(), null, null, false, VALID_COST,
                UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID(), purchaseId
        );

        assertEquals(purchaseId, item.originPurchaseId());
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
    void shouldRejectSelfParentReference() {
        final UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> new Item(
                id, UUID.randomUUID(), ItemStatus.AVAILABLE, id, false,
                VALID_COST, null, null, null, null, null
        ));
    }

    @Test
    void shouldAllowItemWithChildrenAndParent() {
        final Item item = Item.create(
                UUID.randomUUID(), ItemStatus.OPENED, UUID.randomUUID(), true,
                VALID_COST, null, null, null, null
        );
        assertTrue(item.hasChildren());
        assertNotNull(item.parentItemId());
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
