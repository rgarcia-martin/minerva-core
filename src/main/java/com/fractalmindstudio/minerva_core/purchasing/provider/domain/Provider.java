package com.fractalmindstudio.minerva_core.purchasing.provider.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.util.UUID;

public record Provider(
        UUID id,
        String businessName,
        String taxIdentifier,
        String address,
        String phone,
        String email,
        boolean appliesSurcharge
) {

    public static final String FIELD_ID = "provider.id";
    public static final String FIELD_BUSINESS_NAME = "provider.businessName";
    public static final String FIELD_TAX_IDENTIFIER = "provider.taxIdentifier";

    public Provider {
        DomainRules.requireNonNull(id, FIELD_ID);
        businessName = DomainRules.requireNonBlank(businessName, FIELD_BUSINESS_NAME);
        taxIdentifier = DomainRules.requireNonBlank(taxIdentifier, FIELD_TAX_IDENTIFIER);
    }

    public static Provider create(
            final String businessName,
            final String taxIdentifier,
            final String address,
            final String phone,
            final String email,
            final boolean appliesSurcharge
    ) {
        return new Provider(UUID.randomUUID(), businessName, taxIdentifier, address, phone, email, appliesSurcharge);
    }
}
