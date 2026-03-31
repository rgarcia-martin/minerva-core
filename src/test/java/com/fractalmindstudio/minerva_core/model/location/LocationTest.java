package com.fractalmindstudio.minerva_core.model.location;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocationTest {

    private static final UUID LOCATION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String NAME = "Main Warehouse";
    private static final String DESCRIPTION = "Primary storage area";
    private static final String UPDATED_DESCRIPTION = "Secondary storage area";

    @Test
    void shouldCreateLocationWithDefaultValuesAndGeneratedId() {
        Location location = new Location();

        assertThat(location.getId()).isNotNull();
        assertThat(location.getName()).isNull();
        assertThat(location.getDescription()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        Location location = new Location();

        location.setName(NAME);
        location.setDescription(DESCRIPTION);

        assertThat(location.getName()).isEqualTo(NAME);
        assertThat(location.getDescription()).isEqualTo(DESCRIPTION);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        Location first = createLocation();
        Location second = createLocation();

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setDescription(UPDATED_DESCRIPTION);

        assertThat(first).isNotEqualTo(second);
    }

    private Location createLocation() {
        Location location = new Location();
        location.setId(LOCATION_ID);
        location.setName(NAME);
        location.setDescription(DESCRIPTION);
        return location;
    }
}
