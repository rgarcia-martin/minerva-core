package com.fractalmindstudio.minerva_core.model.identity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String NAME = "Ada";
    private static final String LAST_NAME = "Lovelace";
    private static final String ADDRESS = "42 Analytical Engine Street";
    private static final String UPDATED_ADDRESS = "84 Analytical Engine Street";

    @Test
    void shouldCreateUserWithDefaultValuesAndGeneratedId() {
        User user = new User();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getAddress()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        User user = new User();

        user.setName(NAME);
        user.setLastName(LAST_NAME);
        user.setAddress(ADDRESS);

        assertThat(user.getName()).isEqualTo(NAME);
        assertThat(user.getLastName()).isEqualTo(LAST_NAME);
        assertThat(user.getAddress()).isEqualTo(ADDRESS);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        User first = createUser();
        User second = createUser();

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setAddress(UPDATED_ADDRESS);

        assertThat(first).isNotEqualTo(second);
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setName(NAME);
        user.setLastName(LAST_NAME);
        user.setAddress(ADDRESS);
        return user;
    }
}
