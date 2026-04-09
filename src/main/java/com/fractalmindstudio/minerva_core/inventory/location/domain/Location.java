package com.fractalmindstudio.minerva_core.inventory.location.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.util.UUID;

public record Location(
        UUID id,
        String name,
        String description
) {

    public static final String FIELD_ID = "location.id";
    public static final String FIELD_NAME = "location.name";

    public Location {
        DomainRules.requireNonNull(id, FIELD_ID);
        name = DomainRules.requireNonBlank(name, FIELD_NAME);
    }

    public static Location create(final String name, final String description) {
        return new Location(UUID.randomUUID(), name, description);
    }
}
