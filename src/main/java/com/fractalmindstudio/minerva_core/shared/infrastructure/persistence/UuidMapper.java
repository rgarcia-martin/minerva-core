package com.fractalmindstudio.minerva_core.shared.infrastructure.persistence;

import java.util.UUID;

public final class UuidMapper {

    private UuidMapper() {
    }

    public static String toString(final UUID value) {
        return value == null ? null : value.toString();
    }

    public static UUID fromString(final String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }
}
