package com.fractalmindstudio.minerva_core.identity.user.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.util.Set;
import java.util.UUID;

public record User(
        UUID id,
        String name,
        String lastName,
        String email,
        String passwordHash,
        String address,
        Set<Role> roles,
        boolean active
) {

    public static final String FIELD_ID = "user.id";
    public static final String FIELD_NAME = "user.name";
    public static final String FIELD_LAST_NAME = "user.lastName";
    public static final String FIELD_EMAIL = "user.email";
    public static final String FIELD_PASSWORD_HASH = "user.passwordHash";
    public static final String FIELD_ROLES = "user.roles";

    public User {
        DomainRules.requireNonNull(id, FIELD_ID);
        name = DomainRules.requireNonBlank(name, FIELD_NAME);
        lastName = DomainRules.requireNonBlank(lastName, FIELD_LAST_NAME);
        email = DomainRules.normalizeEmail(email, FIELD_EMAIL);
        passwordHash = DomainRules.requireNonBlank(passwordHash, FIELD_PASSWORD_HASH);
        address = DomainRules.trimToNull(address);
        DomainRules.requireNonNull(roles, FIELD_ROLES);
        if (roles.isEmpty()) {
            throw new IllegalArgumentException(FIELD_ROLES + " must not be empty");
        }
        roles = Set.copyOf(roles);
    }

    public static User create(
            final String name,
            final String lastName,
            final String email,
            final String passwordHash,
            final String address,
            final Set<Role> roles
    ) {
        return new User(UUID.randomUUID(), name, lastName, email, passwordHash, address, roles, true);
    }

    public User deactivate() {
        return new User(id, name, lastName, email, passwordHash, address, roles, false);
    }
}
