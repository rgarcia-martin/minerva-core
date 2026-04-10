package com.fractalmindstudio.minerva_core.catalog.tax.interfaces.rest;

import com.fractalmindstudio.minerva_core.catalog.tax.application.TaxService;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.Tax;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
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
class TaxControllerTest {

    private static final String BASE_PATH = TaxController.BASE_PATH;

    private MockMvc mockMvc;

    @Mock
    private TaxService taxService;

    @InjectMocks
    private TaxController taxController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taxController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateTax() throws Exception {
        final var tax = Tax.create("IVA", new BigDecimal("21"), new BigDecimal("5.2"));
        when(taxService.create(eq("IVA"), any(BigDecimal.class), any(BigDecimal.class))).thenReturn(tax);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "IVA", "rate": 21, "surchargeRate": 5.2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tax.id().toString()))
                .andExpect(jsonPath("$.description").value("IVA"));
    }

    @Test
    void shouldFindAllTaxes() throws Exception {
        final var taxes = List.of(
                Tax.create("IVA", new BigDecimal("21"), new BigDecimal("5.2")),
                Tax.create("Reduced", new BigDecimal("10"), new BigDecimal("1.4"))
        );
        when(taxService.findAll()).thenReturn(taxes);

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetTaxById() throws Exception {
        final var tax = Tax.create("IVA", new BigDecimal("21"), new BigDecimal("5.2"));
        when(taxService.getById(tax.id())).thenReturn(tax);

        mockMvc.perform(get(BASE_PATH + "/{id}", tax.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("IVA"));
    }

    @Test
    void shouldUpdateTax() throws Exception {
        final var id = UUID.randomUUID();
        final var updated = new Tax(id, "Updated IVA", new BigDecimal("21"), new BigDecimal("5.2"));
        when(taxService.update(eq(id), eq("Updated IVA"), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(updated);

        mockMvc.perform(put(BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Updated IVA", "rate": 21, "surchargeRate": 5.2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated IVA"));
    }

    @Test
    void shouldDeleteTax() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(taxService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenTaxNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(taxService.getById(id)).thenThrow(new NotFoundException("tax", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("tax with id " + id + " was not found"));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTax() throws Exception {
        final var id = UUID.randomUUID();
        doThrow(new NotFoundException("tax", id)).when(taxService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenDescriptionIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "", "rate": 21, "surchargeRate": 5.2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenRateIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "IVA", "surchargeRate": 5.2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenRateIsNegative() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "IVA", "rate": -1, "surchargeRate": 5.2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenBodyIsNotReadable() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenPathVariableIsInvalidUuid() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/{id}", "invalid"))
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
        when(taxService.create(eq("IVA"), any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("invalid rate"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "IVA", "rate": 21, "surchargeRate": 5.2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid rate"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(taxService.findAll()).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected application error"));
    }
}
