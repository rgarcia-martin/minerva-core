package com.fractalmindstudio.minerva_core.inventory.location.interfaces.rest;

import com.fractalmindstudio.minerva_core.inventory.location.application.LocationService;
import com.fractalmindstudio.minerva_core.inventory.location.domain.Location;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import com.fractalmindstudio.minerva_core.shared.interfaces.rest.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private static final String BASE_PATH = LocationController.BASE_PATH;

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateLocation() throws Exception {
        final var location = Location.create("Warehouse A", "Main warehouse");
        when(locationService.create("Warehouse A", "Main warehouse")).thenReturn(location);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Warehouse A", "description": "Main warehouse"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(location.id().toString()))
                .andExpect(jsonPath("$.name").value("Warehouse A"))
                .andExpect(jsonPath("$.description").value("Main warehouse"));
    }

    @Test
    void shouldFindAllLocations() throws Exception {
        final var locations = List.of(
                Location.create("A", "First"),
                Location.create("B", "Second")
        );
        when(locationService.findAll()).thenReturn(locations);

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void shouldGetLocationById() throws Exception {
        final var location = Location.create("Warehouse A", "Main");
        when(locationService.getById(location.id())).thenReturn(location);

        mockMvc.perform(get(BASE_PATH + "/{id}", location.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Warehouse A"));
    }

    @Test
    void shouldUpdateLocation() throws Exception {
        final var id = UUID.randomUUID();
        final var updated = new Location(id, "Updated", "Updated desc");
        when(locationService.update(eq(id), eq("Updated"), eq("Updated desc"))).thenReturn(updated);

        mockMvc.perform(put(BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated", "description": "Updated desc"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeleteLocation() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(locationService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenLocationNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(locationService.getById(id)).thenThrow(new NotFoundException("location", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("location with id " + id + " was not found"));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentLocation() throws Exception {
        final var id = UUID.randomUUID();
        doThrow(new NotFoundException("location", id)).when(locationService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "description": "desc"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenNameIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "desc"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenBodyIsNotReadable() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenPathVariableIsInvalidUuid() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn415WhenMediaTypeIsUnsupported() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=test"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void shouldReturn400WhenServiceThrowsIllegalArgument() throws Exception {
        when(locationService.create("bad", "desc"))
                .thenThrow(new IllegalArgumentException("invalid data"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "bad", "description": "desc"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("invalid data"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(locationService.findAll()).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected application error"));
    }
}
