package com.fractalmindstudio.minerva_core.payment.paymentmethod.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.util.UUID;

public record PaymentMethod(
        UUID id,
        String name,
        PaymentMethodType type,
        String configuration
) {

    public static final String FIELD_ID = "paymentMethod.id";
    public static final String FIELD_NAME = "paymentMethod.name";
    public static final String FIELD_TYPE = "paymentMethod.type";

    public PaymentMethod {
        DomainRules.requireNonNull(id, FIELD_ID);
        name = DomainRules.requireNonBlank(name, FIELD_NAME);
        DomainRules.requireNonNull(type, FIELD_TYPE);
    }

    public static PaymentMethod create(
            final String name,
            final PaymentMethodType type,
            final String configuration
    ) {
        return new PaymentMethod(UUID.randomUUID(), name, type, configuration);
    }
}
