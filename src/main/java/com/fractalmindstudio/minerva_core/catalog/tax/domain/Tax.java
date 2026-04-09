package com.fractalmindstudio.minerva_core.catalog.tax.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.util.UUID;

public record Tax(
        UUID id,
        String description,
        BigDecimal rate,
        BigDecimal surchargeRate
) {

    public static final String FIELD_ID = "tax.id";
    public static final String FIELD_DESCRIPTION = "tax.description";
    public static final String FIELD_RATE = "tax.rate";
    public static final String FIELD_SURCHARGE_RATE = "tax.surchargeRate";

    public Tax {
        DomainRules.requireNonNull(id, FIELD_ID);
        description = DomainRules.requireNonBlank(description, FIELD_DESCRIPTION);
        DomainRules.requirePositiveOrZero(rate, FIELD_RATE);
        rate = DomainRules.scaleRate(rate);
        DomainRules.requirePositiveOrZero(surchargeRate, FIELD_SURCHARGE_RATE);
        surchargeRate = DomainRules.scaleRate(surchargeRate);
    }

    public static Tax create(
            final String description,
            final BigDecimal rate,
            final BigDecimal surchargeRate
    ) {
        return new Tax(UUID.randomUUID(), description, rate, surchargeRate);
    }
}
