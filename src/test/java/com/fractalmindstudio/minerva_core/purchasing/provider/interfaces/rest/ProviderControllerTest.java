package com.fractalmindstudio.minerva_core.purchasing.provider.interfaces.rest;

import com.fractalmindstudio.minerva_core.purchasing.provider.application.ProviderService;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.Provider;
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
class ProviderControllerTest {

    private static final String BASE_PATH = ProviderController.BASE_PATH;

    private MockMvc mockMvc;

    @Mock
    private ProviderService providerService;

    @InjectMocks
    private ProviderController providerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(providerController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateProvider() throws Exception {
        final var provider = Provider.create("Acme Corp", "B12345678", "123 St", "555-1234", "acme@test.com", false);
        when(providerService.create(eq("Acme Corp"), eq("B12345678"), eq("123 St"), eq("555-1234"), eq("acme@test.com"), eq(false)))
                .thenReturn(provider);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "businessName": "Acme Corp", "taxIdentifier": "B12345678",
                                    "address": "123 St", "phone": "555-1234", "email": "acme@test.com",
                                    "appliesSurcharge": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(provider.id().toString()))
                .andExpect(jsonPath("$.businessName").value("Acme Corp"));
    }

    @Test
    void shouldFindAllProviders() throws Exception {
        final var providers = List.of(
                Provider.create("Acme", "B111", null, null, null, false)
        );
        when(providerService.findAll()).thenReturn(providers);

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetProviderById() throws Exception {
        final var provider = Provider.create("Acme", "B111", null, null, null, true);
        when(providerService.getById(provider.id())).thenReturn(provider);

        mockMvc.perform(get(BASE_PATH + "/{id}", provider.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Acme"))
                .andExpect(jsonPath("$.appliesSurcharge").value(true));
    }

    @Test
    void shouldUpdateProvider() throws Exception {
        final var id = UUID.randomUUID();
        final var updated = new Provider(id, "Updated Corp", "B999", "456 Ave", "555-9999", "new@test.com", true);
        when(providerService.update(eq(id), eq("Updated Corp"), eq("B999"), eq("456 Ave"), eq("555-9999"), eq("new@test.com"), eq(true)))
                .thenReturn(updated);

        mockMvc.perform(put(BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "businessName": "Updated Corp", "taxIdentifier": "B999",
                                    "address": "456 Ave", "phone": "555-9999", "email": "new@test.com",
                                    "appliesSurcharge": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Updated Corp"));
    }

    @Test
    void shouldDeleteProvider() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(providerService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenProviderNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(providerService.getById(id)).thenThrow(new NotFoundException("provider", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("provider with id " + id + " was not found"));
    }

    @Test
    void shouldReturn400WhenBusinessNameIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName": "", "taxIdentifier": "B111"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenTaxIdentifierIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName": "Acme"}
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
        mockMvc.perform(get(BASE_PATH + "/{id}", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn415WhenMediaTypeIsUnsupported() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void shouldReturn400WhenServiceThrowsIllegalArgument() throws Exception {
        when(providerService.create(eq("Acme"), eq("B111"), eq("addr"), eq("phone"), eq("email"), eq(false)))
                .thenThrow(new IllegalArgumentException("invalid provider"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "businessName": "Acme", "taxIdentifier": "B111",
                                    "address": "addr", "phone": "phone", "email": "email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid provider"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(providerService.findAll()).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected application error"));
    }
}
