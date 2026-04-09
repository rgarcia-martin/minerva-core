package com.fractalmindstudio.minerva_core.sales.sale.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the Sale domain model.
 * A sale aggregates lines (items and free concepts), tracks the employee
 * who registered it, optionally associates a client, and records the
 * payment method. Total is derived from lines.
 */
class SaleTest {

    private static final String VALID_CODE = "SAL-2026-0001";
    private static final BigDecimal ITEM_PRICE = new BigDecimal("25.00");
    private static final BigDecimal CONCEPT_PRICE = new BigDecimal("0.10");

    // --- Factory method ---

    @Test
    void shouldCreateSaleWithGeneratedIdAndTimestamp() {
        final UUID employeeId = UUID.randomUUID();
        final UUID paymentMethodId = UUID.randomUUID();

        final Sale sale = Sale.create(
                VALID_CODE, employeeId, null, paymentMethodId, List.of()
        );

        assertNotNull(sale.id());
        assertNotNull(sale.createdOn());
        assertEquals(VALID_CODE, sale.code());
        assertEquals(employeeId, sale.employeeId());
        assertEquals(paymentMethodId, sale.paymentMethodId());
    }

    @Test
    void shouldDefaultStateToNew() {
        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        );

        assertEquals(SaleState.NEW, sale.state());
    }

    // --- Optional client ---

    @Test
    void shouldCreateSaleWithClient() {
        final UUID clientId = UUID.randomUUID();

        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), clientId, UUID.randomUUID(), List.of()
        );

        assertEquals(clientId, sale.clientId());
    }

    @Test
    void shouldCreateSaleWithoutClient() {
        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        );

        assertNull(sale.clientId());
    }

    // --- Total calculation ---

    @Test
    void shouldCalculateTotalFromLines() {
        final SaleLine itemLine = SaleLine.createForItem(
                UUID.randomUUID(), ITEM_PRICE, UUID.randomUUID()
        );
        final SaleLine conceptLine = SaleLine.createForFreeConcept(
                UUID.randomUUID(), 3, CONCEPT_PRICE, UUID.randomUUID()
        );

        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(),
                List.of(itemLine, conceptLine)
        );

        // (1 * 25.00) + (3 * 0.10) = 25.00 + 0.30 = 25.30
        assertEquals(new BigDecimal("25.30"), sale.totalAmount());
    }

    @Test
    void shouldReturnZeroTotalWhenNoLines() {
        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        );

        assertEquals(new BigDecimal("0.00"), sale.totalAmount());
    }

    // --- State transitions ---

    @Test
    void shouldConfirmSale() {
        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        );

        final Sale confirmed = sale.confirm();

        assertEquals(SaleState.CONFIRMED, confirmed.state());
        assertEquals(sale.id(), confirmed.id());
    }

    @Test
    void shouldCancelSale() {
        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        );

        final Sale cancelled = sale.cancel();

        assertEquals(SaleState.CANCELLED, cancelled.state());
        assertEquals(sale.id(), cancelled.id());
    }

    // --- Lines immutability ---

    @Test
    void shouldMakeLinesImmutable() {
        final Sale sale = Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        );

        assertThrows(UnsupportedOperationException.class, () ->
                sale.lines().add(SaleLine.createForItem(
                        UUID.randomUUID(), ITEM_PRICE, UUID.randomUUID()
                ))
        );
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> Sale.create(
                "", UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        ));
    }

    @Test
    void shouldRejectNullCode() {
        assertThrows(IllegalArgumentException.class, () -> Sale.create(
                null, UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        ));
    }

    @Test
    void shouldRejectNullEmployeeId() {
        assertThrows(NullPointerException.class, () -> Sale.create(
                VALID_CODE, null, null, UUID.randomUUID(), List.of()
        ));
    }

    @Test
    void shouldRejectNullPaymentMethodId() {
        assertThrows(NullPointerException.class, () -> Sale.create(
                VALID_CODE, UUID.randomUUID(), null, null, List.of()
        ));
    }

    @Test
    void shouldRejectNullLines() {
        assertThrows(NullPointerException.class, () -> Sale.create(
                VALID_CODE, UUID.randomUUID(), null, UUID.randomUUID(), null
        ));
    }

    @Test
    void shouldTrimCode() {
        final Sale sale = Sale.create(
                "  SAL-2026-0001  ", UUID.randomUUID(), null, UUID.randomUUID(), List.of()
        );

        assertEquals(VALID_CODE, sale.code());
    }
}
