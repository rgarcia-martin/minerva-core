package com.fractalmindstudio.minerva_core.identity.user.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the User domain model.
 * A user represents a system employee with authentication credentials,
 * a set of global roles (READ, CREATE, EDIT, DELETE), and an active flag
 * that allows deactivation without losing historical data.
 */
class UserTest {

    private static final String VALID_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";
    private static final String VALID_EMAIL = "john.doe@company.com";
    private static final String VALID_PASSWORD = "hashed_password_abc123";
    private static final String VALID_ADDRESS = "Main Street 42";
    private static final Set<Role> EMPLOYEE_ROLES = Set.of(Role.READ, Role.CREATE);

    // --- Factory method ---

    @Test
    void shouldCreateUserWithGeneratedIdAndActiveByDefault() {
        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        );

        assertNotNull(user.id());
        assertEquals(VALID_NAME, user.name());
        assertEquals(VALID_LAST_NAME, user.lastName());
        assertEquals(VALID_EMAIL, user.email());
        assertEquals(VALID_PASSWORD, user.password());
        assertTrue(user.active());
    }

    // --- Roles ---

    @Test
    void shouldAssignProvidedRoles() {
        final Set<Role> roles = Set.of(Role.READ, Role.CREATE, Role.EDIT);

        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, roles
        );

        assertEquals(roles, user.roles());
    }

    @Test
    void shouldCreateAdminWithAllRoles() {
        final Set<Role> allRoles = Set.of(Role.READ, Role.CREATE, Role.EDIT, Role.DELETE);

        final User admin = User.create(
                "Admin", "System", "admin@company.com",
                VALID_PASSWORD, null, allRoles
        );

        assertTrue(admin.roles().contains(Role.READ));
        assertTrue(admin.roles().contains(Role.CREATE));
        assertTrue(admin.roles().contains(Role.EDIT));
        assertTrue(admin.roles().contains(Role.DELETE));
    }

    @Test
    void shouldRejectNullRoles() {
        assertThrows(NullPointerException.class, () -> User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, null
        ));
    }

    @Test
    void shouldRejectEmptyRoles() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, Set.of()
        ));
    }

    @Test
    void shouldMakeRolesImmutable() {
        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        );

        assertThrows(UnsupportedOperationException.class, () -> user.roles().add(Role.DELETE));
    }

    // --- Active/deactivation ---

    @Test
    void shouldDeactivateUser() {
        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        );

        final User deactivated = user.deactivate();

        assertFalse(deactivated.active());
        assertEquals(user.id(), deactivated.id());
        assertEquals(user.name(), deactivated.name());
    }

    @Test
    void shouldReconstructInactiveUserFromPersistence() {
        final UUID existingId = UUID.randomUUID();

        final User user = new User(
                existingId, VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES, false
        );

        assertFalse(user.active());
        assertEquals(existingId, user.id());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new User(
                null, VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES, true
        ));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                "", VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldRejectBlankLastName() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, "", VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, VALID_LAST_NAME, "",
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> User.create(
                VALID_NAME, VALID_LAST_NAME, VALID_EMAIL,
                "", VALID_ADDRESS, EMPLOYEE_ROLES
        ));
    }

    @Test
    void shouldTrimName() {
        final User user = User.create(
                "  John  ", VALID_LAST_NAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        );

        assertEquals("John", user.name());
    }

    @Test
    void shouldTrimEmail() {
        final User user = User.create(
                VALID_NAME, VALID_LAST_NAME, "  john@company.com  ",
                VALID_PASSWORD, VALID_ADDRESS, EMPLOYEE_ROLES
        );

        assertEquals("john@company.com", user.email());
    }
}
