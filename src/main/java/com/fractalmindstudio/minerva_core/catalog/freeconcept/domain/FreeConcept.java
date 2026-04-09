package com.fractalmindstudio.minerva_core.catalog.freeconcept.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.util.UUID;

public record FreeConcept(
        UUID id,
        String name,
        String barcode,
        BigDecimal price,
        UUID taxId,
        String description
) {

    public static final String FIELD_ID = "freeconcept.id";
    public static final String FIELD_NAME = "freeconcept.name";
    public static final String FIELD_BARCODE = "freeconcept.barcode";
    public static final String FIELD_PRICE = "freeconcept.price";
    public static final String FIELD_TAX_ID = "freeconcept.taxId";
    public static final String FIELD_DESCRIPTION = "freeconcept.description";

    public FreeConcept {
        DomainRules.requireNonNull(id, FIELD_ID);
        name = DomainRules.requireNonBlank(name, FIELD_NAME);
        barcode = DomainRules.requireNonBlank(barcode, FIELD_BARCODE);
        DomainRules.requirePositiveOrZero(price, FIELD_PRICE);
        price = DomainRules.scaleMoney(price);
        DomainRules.requireNonNull(taxId, FIELD_TAX_ID);
    }

    public static FreeConcept create(
            final String name,
            final String barcode,
            final BigDecimal price,
            final UUID taxId,
            final String description
    ){
        return new FreeConcept(
                UUID.randomUUID(),
                name,
                barcode,
                price,
                taxId,
                description
        );
    }
}
