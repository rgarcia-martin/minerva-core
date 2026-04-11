package com.fractalmindstudio.minerva_core.inventory.location.application;

import com.fractalmindstudio.minerva_core.inventory.location.domain.Location;
import com.fractalmindstudio.minerva_core.inventory.location.domain.LocationRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    @Test
    void shouldCreateLocation() {
        when(locationRepository.save(any(Location.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = locationService.create("Warehouse", "Main warehouse");

        assertThat(result.name()).isEqualTo("Warehouse");
        assertThat(result.description()).isEqualTo("Main warehouse");
        assertThat(result.id()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Warehouse");
    }

    @Test
    void shouldGetLocationById() {
        final var location = Location.create("Warehouse", "desc");
        when(locationRepository.findById(location.id())).thenReturn(Optional.of(location));

        final var result = locationService.getById(location.id());

        assertThat(result).isEqualTo(location);
    }

    @Test
    void shouldThrowNotFoundWhenLocationDoesNotExist() {
        final var id = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnRepositoryOrderFromFindAll() {
        final var a = Location.create("A-location", null);
        final var b = Location.create("B-location", null);
        when(locationRepository.findAll()).thenReturn(List.of(a, b));

        final var result = locationService.findAll();

        assertThat(result).extracting(Location::name).containsExactly("A-location", "B-location");
    }

    @Test
    void shouldUpdateLocation() {
        final var id = UUID.randomUUID();
        final var existing = new Location(id, "Old", "Old desc");
        when(locationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(locationRepository.save(any(Location.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = locationService.update(id, "New", "New desc");

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("New");
        assertThat(result.description()).isEqualTo("New desc");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentLocation() {
        final var id = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.update(id, "Name", "Desc"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteLocation() {
        final var id = UUID.randomUUID();
        final var location = new Location(id, "Warehouse", null);
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        locationService.delete(id);

        verify(locationRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentLocation() {
        final var id = UUID.randomUUID();
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
