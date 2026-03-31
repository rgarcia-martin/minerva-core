package com.fractalmindstudio.minerva_core.model.item;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemStatusTest {

    private static final String SOLD = "SOLD";

    @Test
    void shouldExposeAllExpectedValuesInDeclarationOrder() {
        assertThat(ItemStatus.values()).containsExactly(
                ItemStatus.AVAILABLE,
                ItemStatus.SOLD,
                ItemStatus.RESERVED,
                ItemStatus.OPENED
        );
    }

    @Test
    void shouldResolveValueByName() {
        assertThat(ItemStatus.valueOf(SOLD)).isEqualTo(ItemStatus.SOLD);
    }
}
