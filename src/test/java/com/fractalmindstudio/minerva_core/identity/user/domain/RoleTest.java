package com.fractalmindstudio.minerva_core.identity.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the Role enum.
 * Roles represent global permissions assigned to users:
 * READ, CREATE, EDIT, DELETE.
 */
class RoleTest {

    private static final int EXPECTED_ROLE_COUNT = 4;

    @Test
    void shouldHaveExactlyFourRoles() {
        assertEquals(EXPECTED_ROLE_COUNT, Role.values().length);
    }

    @Test
    void shouldContainAllRequiredRoles() {
        assertEquals(Role.READ, Role.valueOf("READ"));
        assertEquals(Role.CREATE, Role.valueOf("CREATE"));
        assertEquals(Role.EDIT, Role.valueOf("EDIT"));
        assertEquals(Role.DELETE, Role.valueOf("DELETE"));
    }
}
