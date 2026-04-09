package com.fractalmindstudio.minerva_core.purchasing.purchase.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Purchase domain model.
 * A purchase represents a delivery note (albaran) registered from a provider's
 * paper document. It contains lines, supports regular and deposit modes,
 * and auto-calculates its total cost.
 */
class PurchaseTest {

    private static final String VALID_CODE = "PUR-2026-0001";
    private static final String VALID_PROVIDER_CODE = "ALB-PROV-4521";
    private static final BigDecimal VALID_BUY_PRICE = new BigDecimal("10.00");
    private static final BigDecimal VALID_PROFIT_MARGIN = new BigDecimal("25.0000");
    private static final int VALID_QUANTITY = 2;

    // --- Factory method ---

    @Test
    void shouldCreatePurchaseWithGeneratedIdAndTimestamp() {
        final UUID providerId = UUID.randomUUID();
        final UUID locationId = UUID.randomUUID();

        final Purchase purchase = Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                providerId, locationId, false, List.of()
        );

        assertNotNull(purchase.id());
        assertNotNull(purchase.createdOn());
        assertEquals(VALID_CODE, purchase.code());
        assertEquals(VALID_PROVIDER_CODE, purchase.providerCode());
        assertEquals(providerId, purchase.providerId());
        assertEquals(locationId, purchase.locationId());
    }

    @Test
    void shouldDefaultStateToNew() {
        final Purchase purchase = Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false, List.of()
        );

        assertEquals(PurchaseState.NEW, purchase.state());
    }

    // --- Deposit mode ---

    @Test
    void shouldCreateDepositPurchaseWithExpirationDate() {
        final LocalDateTime expirationDate = LocalDateTime.of(2026, 6, 30, 0, 0);

        final Purchase purchase = Purchase.create(
                null, expirationDate, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), true, List.of()
        );

        assertTrue(purchase.deposit());
        assertEquals(expirationDate, purchase.finishDate());
    }

    @Test
    void shouldCreateRegularPurchaseWithoutExpirationDate() {
        final Purchase purchase = Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false, List.of()
        );

        assertFalse(purchase.deposit());
        assertNull(purchase.finishDate());
    }

    // --- Total cost calculation ---

    @Test
    void shouldRecalculateTotalCostFromLines() {
        final PurchaseLine firstLine = PurchaseLine.create(
                UUID.randomUUID(), 2, new BigDecimal("10.00"),
                VALID_PROFIT_MARGIN, UUID.randomUUID()
        );
        final PurchaseLine secondLine = PurchaseLine.create(
                UUID.randomUUID(), 3, new BigDecimal("5.00"),
                VALID_PROFIT_MARGIN, UUID.randomUUID()
        );

        final Purchase purchase = Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false,
                List.of(firstLine, secondLine)
        );

        // (2 * 10.00) + (3 * 5.00) = 20.00 + 15.00 = 35.00
        assertEquals(new BigDecimal("35.00"), purchase.totalCost());
    }

    @Test
    void shouldReturnZeroTotalCostWhenNoLines() {
        final Purchase purchase = Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false, List.of()
        );

        assertEquals(new BigDecimal("0.00"), purchase.totalCost());
    }

    // --- Lines immutability ---

    @Test
    void shouldMakeLinesImmutable() {
        final Purchase purchase = Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false, List.of()
        );

        assertThrows(UnsupportedOperationException.class, () ->
                purchase.lines().add(PurchaseLine.create(
                        UUID.randomUUID(), 1, VALID_BUY_PRICE,
                        VALID_PROFIT_MARGIN, UUID.randomUUID()
                ))
        );
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> Purchase.create(
                null, null, null, "", VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false, List.of()
        ));
    }

    @Test
    void shouldRejectBlankProviderCode() {
        assertThrows(IllegalArgumentException.class, () -> Purchase.create(
                null, null, null, VALID_CODE, "",
                UUID.randomUUID(), UUID.randomUUID(), false, List.of()
        ));
    }

    @Test
    void shouldRejectNullProviderId() {
        assertThrows(NullPointerException.class, () -> Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                null, UUID.randomUUID(), false, List.of()
        ));
    }

    @Test
    void shouldRejectNullLocationId() {
        assertThrows(NullPointerException.class, () -> Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), null, false, List.of()
        ));
    }

    @Test
    void shouldRejectNullLines() {
        assertThrows(NullPointerException.class, () -> Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false, null
        ));
    }

    // --- State transitions ---

    @Test
    void shouldTransitionToPaid() {
        final Purchase purchase = Purchase.create(
                null, null, null, VALID_CODE, VALID_PROVIDER_CODE,
                UUID.randomUUID(), UUID.randomUUID(), false, List.of()
        );

        final Purchase paid = purchase.markAsPaid();

        assertEquals(PurchaseState.PAID, paid.state());
        assertEquals(purchase.id(), paid.id());
    }
}
