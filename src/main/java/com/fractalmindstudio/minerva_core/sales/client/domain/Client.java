package com.fractalmindstudio.minerva_core.sales.client.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.util.UUID;

public record Client(
        UUID id,
        String name,
        String lastName,
        String email,
        String phone,
        String taxIdentifier
) {

    public static final String FIELD_ID = "client.id";
    public static final String FIELD_NAME = "client.name";
    public static final String FIELD_LAST_NAME = "client.lastName";

    public Client {
        DomainRules.requireNonNull(id, FIELD_ID);
        name = DomainRules.requireNonBlank(name, FIELD_NAME);
        lastName = DomainRules.requireNonBlank(lastName, FIELD_LAST_NAME);
    }

    public static Client create(
            final String name,
            final String lastName,
            final String email,
            final String phone,
            final String taxIdentifier
    ) {
        return new Client(UUID.randomUUID(), name, lastName, email, phone, taxIdentifier);
    }
}
