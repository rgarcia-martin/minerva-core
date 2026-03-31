package com.fractalmindstudio.minerva_core.model.purchase;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseStateTest {

    private static final String FINISHED = "FINISHED";

    @Test
    void shouldExposeAllExpectedValuesInDeclarationOrder() {
        assertThat(PurchaseState.values()).containsExactly(
                PurchaseState.NEW,
                PurchaseState.RECEIVED,
                PurchaseState.OUTDATED,
                PurchaseState.PAID,
                PurchaseState.FINISHED
        );
    }

    @Test
    void shouldResolveValueByName() {
        assertThat(PurchaseState.valueOf(FINISHED)).isEqualTo(PurchaseState.FINISHED);
    }
}
