package com.fractalmindstudio.minerva_core.model.tax;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaxTest {

    private static final UUID TAX_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID SPECIAL_TAX_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String DESCRIPTION = "Standard VAT";
    private static final String UPDATED_DESCRIPTION = "Reduced VAT";
    private static final Float TAX_VALUE = 21.0F;
    private static final Float SPECIAL_TAX_VALUE = 7.0F;

    @Test
    void shouldCreateTaxWithDefaultValuesAndGeneratedId() {
        Tax tax = new Tax();

        assertThat(tax.getId()).isNotNull();
        assertThat(tax.getDescription()).isNull();
        assertThat(tax.getTax()).isNull();
        assertThat(tax.getSpecialTax()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        Tax specialTax = createSpecialTax();
        Tax tax = new Tax();

        tax.setDescription(DESCRIPTION);
        tax.setTax(TAX_VALUE);
        tax.setSpecialTax(specialTax);

        assertThat(tax.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(tax.getTax()).isEqualTo(TAX_VALUE);
        assertThat(tax.getSpecialTax()).isEqualTo(specialTax);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        Tax specialTax = createSpecialTax();
        Tax first = createTax(specialTax);
        Tax second = createTax(specialTax);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setDescription(UPDATED_DESCRIPTION);

        assertThat(first).isNotEqualTo(second);
    }

    private Tax createTax(Tax specialTax) {
        Tax tax = new Tax();
        tax.setId(TAX_ID);
        tax.setDescription(DESCRIPTION);
        tax.setTax(TAX_VALUE);
        tax.setSpecialTax(specialTax);
        return tax;
    }

    private Tax createSpecialTax() {
        Tax specialTax = new Tax();
        specialTax.setId(SPECIAL_TAX_ID);
        specialTax.setDescription(UPDATED_DESCRIPTION);
        specialTax.setTax(SPECIAL_TAX_VALUE);
        return specialTax;
    }
}
