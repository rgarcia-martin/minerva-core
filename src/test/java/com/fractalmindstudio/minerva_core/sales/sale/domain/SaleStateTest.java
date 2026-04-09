package com.fractalmindstudio.minerva_core.sales.sale.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the SaleState enum.
 * A sale transitions through: NEW -> CONFIRMED -> optionally CANCELLED.
 */
class SaleStateTest {

    private static final int EXPECTED_STATE_COUNT = 3;

    @Test
    void shouldHaveExactlyThreeStates() {
        assertEquals(EXPECTED_STATE_COUNT, SaleState.values().length);
    }

    @Test
    void shouldContainAllRequiredStates() {
        assertEquals(SaleState.NEW, SaleState.valueOf("NEW"));
        assertEquals(SaleState.CONFIRMED, SaleState.valueOf("CONFIRMED"));
        assertEquals(SaleState.CANCELLED, SaleState.valueOf("CANCELLED"));
    }
}
