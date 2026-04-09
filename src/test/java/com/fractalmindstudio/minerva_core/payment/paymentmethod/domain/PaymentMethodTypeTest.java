package com.fractalmindstudio.minerva_core.payment.paymentmethod.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the PaymentMethodType enum.
 * Ensures all required payment types are present: CASH, CARD, GATEWAY.
 */
class PaymentMethodTypeTest {

    private static final int EXPECTED_TYPE_COUNT = 3;

    @Test
    void shouldHaveExactlyThreeTypes() {
        assertEquals(EXPECTED_TYPE_COUNT, PaymentMethodType.values().length);
    }

    @Test
    void shouldContainAllRequiredTypes() {
        assertEquals(PaymentMethodType.CASH, PaymentMethodType.valueOf("CASH"));
        assertEquals(PaymentMethodType.CARD, PaymentMethodType.valueOf("CARD"));
        assertEquals(PaymentMethodType.GATEWAY, PaymentMethodType.valueOf("GATEWAY"));
    }
}
