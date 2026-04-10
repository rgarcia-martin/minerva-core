package com.fractalmindstudio.minerva_core.sales.sale.interfaces.rest;

import com.fractalmindstudio.minerva_core.sales.sale.application.SaleService;
import com.fractalmindstudio.minerva_core.sales.sale.domain.Sale;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleLine;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    private static final String BASE_PATH = SaleController.BASE_PATH;
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();
    private static final UUID PAYMENT_METHOD_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID TAX_ID = UUID.randomUUID();

    private MockMvc mockMvc;

    @Mock
    private SaleService saleService;

    @InjectMocks
    private SaleController saleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(saleController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private String validSaleJson() {
        return """
                {
                    "code": "SALE-001",
                    "employeeId": "%s",
                    "paymentMethodId": "%s",
                    "lines": [
                        {
                            "itemId": "%s",
                            "unitPrice": 25.00,
                            "taxId": "%s"
                        }
                    ]
                }
                """.formatted(EMPLOYEE_ID, PAYMENT_METHOD_ID, ITEM_ID, TAX_ID);
    }

    @Test
    void shouldCreateSale() throws Exception {
        final var line = SaleLine.createForItem(ITEM_ID, new BigDecimal("25"), TAX_ID);
        final var sale = Sale.create("SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of(line));
        when(saleService.create(eq("SALE-001"), eq(EMPLOYEE_ID), isNull(), eq(PAYMENT_METHOD_ID), anyList()))
                .thenReturn(sale);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSaleJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sale.id().toString()))
                .andExpect(jsonPath("$.code").value("SALE-001"))
                .andExpect(jsonPath("$.state").value("NEW"))
                .andExpect(jsonPath("$.lines", hasSize(1)));
    }

    @Test
    void shouldFindAllSales() throws Exception {
        final var sale = Sale.create("SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of());
        when(saleService.findAll()).thenReturn(List.of(sale));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetSaleById() throws Exception {
        final var sale = Sale.create("SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of());
        when(saleService.getById(sale.id())).thenReturn(sale);

        mockMvc.perform(get(BASE_PATH + "/{id}", sale.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SALE-001"));
    }

    @Test
    void shouldDeleteSale() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(saleService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenSaleNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(saleService.getById(id)).thenThrow(new NotFoundException("sale", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("sale with id " + id + " was not found"));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentSale() throws Exception {
        final var id = UUID.randomUUID();
        doThrow(new NotFoundException("sale", id)).when(saleService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenCodeIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "",
                                    "employeeId": "%s",
                                    "paymentMethodId": "%s"
                                }
                                """.formatted(EMPLOYEE_ID, PAYMENT_METHOD_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenEmployeeIdIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "SALE-001",
                                    "paymentMethodId": "%s"
                                }
                                """.formatted(PAYMENT_METHOD_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenPaymentMethodIdIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "SALE-001",
                                    "employeeId": "%s"
                                }
                                """.formatted(EMPLOYEE_ID)))
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
        when(saleService.create(eq("SALE-001"), eq(EMPLOYEE_ID), isNull(), eq(PAYMENT_METHOD_ID), anyList()))
                .thenThrow(new IllegalArgumentException("invalid sale"));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSaleJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid sale"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(saleService.findAll()).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected application error"));
    }
}
