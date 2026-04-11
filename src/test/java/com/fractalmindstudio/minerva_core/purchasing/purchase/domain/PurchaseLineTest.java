package com.fractalmindstudio.minerva_core.purchasing.purchase.domain;

import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the PurchaseLine domain model.
 */
class PurchaseLineTest {

    private static final int VALID_QUANTITY = 2;
    private static final BigDecimal VALID_BUY_PRICE = new BigDecimal("10.00");
    private static final BigDecimal VALID_PROFIT_MARGIN = new BigDecimal("25.0000");

    @Test
    void shouldCreatePurchaseLineWithGeneratedId() {
        final UUID articleId = UUID.randomUUID();
        final UUID taxId = UUID.randomUUID();

        final PurchaseLine line = PurchaseLine.create(
                articleId, VALID_QUANTITY, VALID_BUY_PRICE, VALID_PROFIT_MARGIN, taxId
        );

        assertNotNull(line.id());
        assertEquals(articleId, line.articleId());
        assertEquals(taxId, line.taxId());
        assertEquals(VALID_QUANTITY, line.quantity());
        assertEquals(ItemStatus.AVAILABLE, line.itemStatus());
        assertFalse(line.hasChildren());
    }

    @Test
    void shouldAllowExplicitInventoryFlags() {
        final PurchaseLine line = PurchaseLine.create(
                UUID.randomUUID(),
                1,
                new BigDecimal("16.00"),
                BigDecimal.ZERO,
                UUID.randomUUID(),
                ItemStatus.OPENED,
                true
        );

        assertEquals(ItemStatus.OPENED, line.itemStatus());
        assertTrue(line.hasChildren());
    }

    @Test
    void shouldScaleBuyPriceToTwoDecimalPlaces() {
        final PurchaseLine line = PurchaseLine.create(
                UUID.randomUUID(), VALID_QUANTITY, new BigDecimal("10.125"),
                VALID_PROFIT_MARGIN, UUID.randomUUID()
        );

        assertEquals(new BigDecimal("10.13"), line.buyPrice());
    }

    @Test
    void shouldScaleProfitMarginToFourDecimalPlaces() {
        final PurchaseLine line = PurchaseLine.create(
                UUID.randomUUID(), VALID_QUANTITY, VALID_BUY_PRICE,
                new BigDecimal("25.12345"), UUID.randomUUID()
        );

        assertEquals(new BigDecimal("25.1235"), line.profitMargin());
    }

    @Test
    void shouldAcceptZeroProfitMargin() {
        final PurchaseLine line = PurchaseLine.create(
                UUID.randomUUID(), VALID_QUANTITY, VALID_BUY_PRICE,
                BigDecimal.ZERO, UUID.randomUUID()
        );

        assertEquals(new BigDecimal("0.0000"), line.profitMargin());
    }

    @Test
    void shouldCalculateLineTotalFromQuantityAndBuyPrice() {
        final PurchaseLine line = PurchaseLine.create(
                UUID.randomUUID(), 3, new BigDecimal("10.00"),
                VALID_PROFIT_MARGIN, UUID.randomUUID()
        );

        assertEquals(new BigDecimal("30.00"), line.lineTotal());
    }

    @Test
    void shouldRejectNullArticleId() {
        assertThrows(NullPointerException.class, () -> PurchaseLine.create(
                null, VALID_QUANTITY, VALID_BUY_PRICE, VALID_PROFIT_MARGIN, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> PurchaseLine.create(
                UUID.randomUUID(), 0, VALID_BUY_PRICE, VALID_PROFIT_MARGIN, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> PurchaseLine.create(
                UUID.randomUUID(), -1, VALID_BUY_PRICE, VALID_PROFIT_MARGIN, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNegativeBuyPrice() {
        assertThrows(IllegalArgumentException.class, () -> PurchaseLine.create(
                UUID.randomUUID(), VALID_QUANTITY, new BigDecimal("-0.01"),
                VALID_PROFIT_MARGIN, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNullBuyPrice() {
        assertThrows(NullPointerException.class, () -> PurchaseLine.create(
                UUID.randomUUID(), VALID_QUANTITY, null,
                VALID_PROFIT_MARGIN, UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNegativeProfitMargin() {
        assertThrows(IllegalArgumentException.class, () -> PurchaseLine.create(
                UUID.randomUUID(), VALID_QUANTITY, VALID_BUY_PRICE,
                new BigDecimal("-1.00"), UUID.randomUUID()
        ));
    }

    @Test
    void shouldRejectNullTaxId() {
        assertThrows(NullPointerException.class, () -> PurchaseLine.create(
                UUID.randomUUID(), VALID_QUANTITY, VALID_BUY_PRICE,
                VALID_PROFIT_MARGIN, null
        ));
    }
}
