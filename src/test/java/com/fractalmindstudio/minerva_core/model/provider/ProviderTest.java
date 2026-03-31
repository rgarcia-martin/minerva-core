package com.fractalmindstudio.minerva_core.model.provider;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderTest {

    private static final UUID PROVIDER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final String NAME = "Acme Supplies";

    @Test
    void shouldCreateProviderWithDefaultValuesAndGeneratedId() {
        Provider provider = new Provider();

        assertThat(provider.getId()).isNotNull();
        assertThat(provider.getName()).isNull();
        assertThat(provider.getSpecialTax()).isFalse();
    }

    @Test
    void shouldStoreAssignedValues() {
        Provider provider = new Provider();

        provider.setName(NAME);
        provider.setSpecialTax(Boolean.TRUE);

        assertThat(provider.getName()).isEqualTo(NAME);
        assertThat(provider.getSpecialTax()).isTrue();
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        Provider first = createProvider(Boolean.TRUE);
        Provider second = createProvider(Boolean.TRUE);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setSpecialTax(Boolean.FALSE);

        assertThat(first).isNotEqualTo(second);
    }

    private Provider createProvider(Boolean specialTax) {
        Provider provider = new Provider();
        provider.setId(PROVIDER_ID);
        provider.setName(NAME);
        provider.setSpecialTax(specialTax);
        return provider;
    }
}
