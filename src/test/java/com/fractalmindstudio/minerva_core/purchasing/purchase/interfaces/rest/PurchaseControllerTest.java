package com.fractalmindstudio.minerva_core.purchasing.purchase.interfaces.rest;

import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.purchasing.purchase.application.PurchaseService;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseState;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    private static final String BASE_PATH = PurchaseController.BASE_PATH;
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final UUID LOCATION_ID = UUID.randomUUID();
    private static final UUID ARTICLE_ID = UUID.randomUUID();
    private static final UUID TAX_ID = UUID.randomUUID();

    private MockMvc mockMvc;

    @Mock
    private PurchaseService purchaseService;

    @InjectMocks
    private PurchaseController purchaseController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(purchaseController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private String validPurchaseJson() {
        return """
                {
                    "code": "PO-001",
                    "providerCode": "PC-001",
                    "providerId": "%s",
                    "locationId": "%s",
                    "deposit": false,
                    "lines": [
                        {
                            "articleId": "%s",
                            "quantity": 5,
                            "buyPrice": 10.00,
                            "profitMargin": 0.20,
                            "taxId": "%s",
                            "itemStatus": "OPENED",
                            "hasChildren": true
                        }
                    ]
                }
                """.formatted(PROVIDER_ID, LOCATION_ID, ARTICLE_ID, TAX_ID);
    }

    @Test
    void shouldCreatePurchase() throws Exception {
        final var line = PurchaseLine.create(ARTICLE_ID, 5, new BigDecimal("10"), new BigDecimal("0.20"), TAX_ID, ItemStatus.OPENED, true);
        final var purchase = Purchase.create(
                LocalDateTime.now(), null, PurchaseState.NEW, "PO-001", "PC-001",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );
        when(purchaseService.create(any(), any(), any(), eq("PO-001"), eq("PC-001"),
                eq(PROVIDER_ID), eq(LOCATION_ID), eq(false), anyList()))
                .thenReturn(purchase);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPurchaseJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(purchase.id().toString()))
                .andExpect(jsonPath("$.code").value("PO-001"))
                .andExpect(jsonPath("$.lines", hasSize(1)))
                .andExpect(jsonPath("$.lines[0].itemStatus").value("OPENED"))
                .andExpect(jsonPath("$.lines[0].hasChildren").value(true));
    }

    @Test
    void shouldFindAllPurchases() throws Exception {
        final var purchase = Purchase.create(
                LocalDateTime.now(), null, null, "PO-001", "PC-001",
                PROVIDER_ID, LOCATION_ID, false, List.of()
        );
        when(purchaseService.findAll()).thenReturn(List.of(purchase));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetPurchaseById() throws Exception {
        final var purchase = Purchase.create(
                LocalDateTime.now(), null, null, "PO-001", "PC-001",
                PROVIDER_ID, LOCATION_ID, false, List.of()
        );
        when(purchaseService.getById(purchase.id())).thenReturn(purchase);

        mockMvc.perform(get(BASE_PATH + "/{id}", purchase.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PO-001"));
    }

    @Test
    void shouldUpdatePurchase() throws Exception {
        final var id = UUID.randomUUID();
        final var updated = Purchase.create(
                LocalDateTime.now(), null, PurchaseState.RECEIVED, "PO-002", "PC-002",
                PROVIDER_ID, LOCATION_ID, true, List.of()
        );
        when(purchaseService.update(eq(id), any(), any(), any(), eq("PO-002"), eq("PC-002"),
                eq(PROVIDER_ID), eq(LOCATION_ID), eq(true), anyList()))
                .thenReturn(updated);

        mockMvc.perform(put(BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "PO-002", "providerCode": "PC-002",
                                    "providerId": "%s", "locationId": "%s",
                                    "state": "RECEIVED", "deposit": true, "lines": []
                                }
                                """.formatted(PROVIDER_ID, LOCATION_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PO-002"));
    }

    @Test
    void shouldDeletePurchase() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(purchaseService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenPurchaseNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(purchaseService.getById(id)).thenThrow(new NotFoundException("purchase", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("purchase with id " + id + " was not found"));
    }

    @Test
    void shouldReturn400WhenCodeIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "", "providerCode": "PC-001",
                                    "providerId": "%s", "locationId": "%s"
                                }
                                """.formatted(PROVIDER_ID, LOCATION_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenProviderIdIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "PO-001", "providerCode": "PC-001",
                                    "locationId": "%s"
                                }
                                """.formatted(LOCATION_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenLocationIdIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "PO-001", "providerCode": "PC-001",
                                    "providerId": "%s"
                                }
                                """.formatted(PROVIDER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenProviderCodeIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "PO-001",
                                    "providerId": "%s", "locationId": "%s"
                                }
                                """.formatted(PROVIDER_ID, LOCATION_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenBodyIsNotReadable() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("broken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenPathVariableIsInvalidUuid() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/{id}", "not-uuid"))
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
        when(purchaseService.create(any(), any(), any(), eq("PO-001"), eq("PC-001"),
                eq(PROVIDER_ID), eq(LOCATION_ID), eq(false), anyList()))
                .thenThrow(new IllegalArgumentException("invalid purchase"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPurchaseJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid purchase"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(purchaseService.findAll()).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected application error"));
    }
}
