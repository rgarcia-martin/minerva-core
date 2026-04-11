package com.fractalmindstudio.minerva_core.identity.user.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private static final String VALID_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";
    private static final String VALID_EMAIL = "john.doe@company.com";
    private static final String VALID_PASSWORD_HASH = "hashed_password_abc123";
    private static final String VALID_ADDRESS = "Main Street 42";
    private static final Set<Role> EMPLOYEE_ROLES = Set.of(Role.READ, Role.CREATE);

    @Test
    void shouldCreateUserWithGeneratedIdAndActiveByDefault() {
        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, EMPLOYEE_ROLES
        );

        assertNotNull(user.id());
        assertEquals(VALID_NAME, user.name());
        assertEquals(VALID_LAST_NAME, user.lastName());
        assertEquals(VALID_EMAIL, user.email());
        assertEquals(VALID_PASSWORD_HASH, user.passwordHash());
        assertTrue(user.active());
    }

    @Test
    void shouldAssignProvidedRoles() {
        final Set<Role> roles = Set.of(Role.READ, Role.CREATE, Role.EDIT);

        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, roles
        );

        assertEquals(roles, user.roles());
    }

    @Test
    void shouldNormalizeEmailAndAddress() {
        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, "  JOHN.DOE@COMPANY.COM  ",
                VALID_PASSWORD_HASH, "  ", EMPLOYEE_ROLES
        );

        assertEquals("john.doe@company.com", user.email());
        assertNull(user.address());
    }

    @Test
    void shouldCreateAdminWithAllRoles() {
        final Set<Role> allRoles = Set.of(Role.READ, Role.CREATE, Role.EDIT, Role.DELETE);

        final User admin = User.create(
                "Admin", "System", "admin@company.com",
                VALID_PASSWORD_HASH, null, allRoles
        );

        assertTrue(admin.roles().contains(Role.READ));
        assertTrue(admin.roles().contains(Role.CREATE));
        assertTrue(admin.roles().contains(Role.EDIT));
        assertTrue(admin.roles().contains(Role.DELETE));
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new User(
                null, VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, EMPLOYEE_ROLES, true
        ));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                "", VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldRejectBlankLastName() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, "", VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, VALID_LAST_NAME, " ",
                VALID_PASSWORD_HASH, VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldRejectBlankPasswordHash() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                " ", VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldRejectEmptyRoles() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, Set.of()
        ));
    }

    @Test
    void shouldDefensivelyCopyRoles() {
        final var mutableRoles = new java.util.HashSet<Role>();
        mutableRoles.add(Role.READ);

        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, mutableRoles
        );
        mutableRoles.add(Role.DELETE);

        assertEquals(Set.of(Role.READ), user.roles());
    }

    @Test
    void shouldDeactivateUser() {
        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD_HASH, VALID_ADDRESS, EMPLOYEE_ROLES
        );

        final User deactivated = user.deactivate();

        assertFalse(deactivated.active());
        assertEquals(user.id(), deactivated.id());
        assertEquals(user.passwordHash(), deactivated.passwordHash());
    }
}
