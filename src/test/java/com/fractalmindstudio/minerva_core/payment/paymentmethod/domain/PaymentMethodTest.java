package com.fractalmindstudio.minerva_core.payment.paymentmethod.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the PaymentMethod domain model.
 * A payment method represents a configured way to receive payments
 * (cash, card linked to a bank account, or payment gateway).
 */
class PaymentMethodTest {

    private static final String VALID_NAME = "Visa Business";
    private static final String VALID_CONFIGURATION = "ES91 2100 0418 4502 0005 1332";

    // --- Factory method ---

    @Test
    void shouldCreatePaymentMethodWithGeneratedId() {
        final PaymentMethod method = PaymentMethod.create(
                VALID_NAME, PaymentMethodType.CARD, VALID_CONFIGURATION
        );

        assertNotNull(method.id());
        assertEquals(VALID_NAME, method.name());
        assertEquals(PaymentMethodType.CARD, method.type());
        assertEquals(VALID_CONFIGURATION, method.configuration());
    }

    // --- Payment types ---

    @Test
    void shouldCreateCashPaymentWithoutConfiguration() {
        final PaymentMethod cash = PaymentMethod.create(
                "Cash", PaymentMethodType.CASH, null
        );

        assertEquals(PaymentMethodType.CASH, cash.type());
        assertNull(cash.configuration());
    }

    @Test
    void shouldCreateCardPaymentWithBankAccount() {
        final PaymentMethod card = PaymentMethod.create(
                VALID_NAME, PaymentMethodType.CARD, VALID_CONFIGURATION
        );

        assertEquals(PaymentMethodType.CARD, card.type());
        assertEquals(VALID_CONFIGURATION, card.configuration());
    }

    @Test
    void shouldCreateGatewayPayment() {
        final PaymentMethod gateway = PaymentMethod.create(
                "PayPal", PaymentMethodType.GATEWAY, "merchant_id_12345"
        );

        assertEquals(PaymentMethodType.GATEWAY, gateway.type());
        assertEquals("merchant_id_12345", gateway.configuration());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new PaymentMethod(
                null, VALID_NAME, PaymentMethodType.CARD, VALID_CONFIGURATION
        ));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethod.create(
                "", PaymentMethodType.CASH, null
        ));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> PaymentMethod.create(
                null, PaymentMethodType.CASH, null
        ));
    }

    @Test
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () -> PaymentMethod.create(
                VALID_NAME, null, VALID_CONFIGURATION
        ));
    }

    @Test
    void shouldTrimName() {
        final PaymentMethod method = PaymentMethod.create(
                "  Visa Business  ", PaymentMethodType.CARD, VALID_CONFIGURATION
        );

        assertEquals(VALID_NAME, method.name());
    }

    // --- Reconstruction from persistence ---

    @Test
    void shouldReconstructPaymentMethodFromPersistence() {
        final UUID existingId = UUID.randomUUID();

        final PaymentMethod method = new PaymentMethod(
                existingId, VALID_NAME, PaymentMethodType.CARD, VALID_CONFIGURATION
        );

        assertEquals(existingId, method.id());
    }
}
