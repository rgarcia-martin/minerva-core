package com.fractalmindstudio.minerva_core.payment.paymentmethod.interfaces.rest;

import com.fractalmindstudio.minerva_core.payment.paymentmethod.application.PaymentMethodService;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethod;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodType;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentMethodControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentMethodService paymentMethodService;

    @InjectMocks
    private PaymentMethodController paymentMethodController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentMethodController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldCreatePaymentMethod() throws Exception {
        final var method = PaymentMethod.create("Cash", PaymentMethodType.CASH, null);
        when(paymentMethodService.create(eq("Cash"), eq(PaymentMethodType.CASH), isNull())).thenReturn(method);

        mockMvc.perform(post(PaymentMethodController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Cash",
                                    "type": "CASH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(method.id().toString()))
                .andExpect(jsonPath("$.type").value("CASH"));
    }

    @Test
    void shouldFindAllPaymentMethods() throws Exception {
        when(paymentMethodService.findAll()).thenReturn(List.of(PaymentMethod.create("Cash", PaymentMethodType.CASH, null)));

        mockMvc.perform(get(PaymentMethodController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetById() throws Exception {
        final var method = PaymentMethod.create("Cash", PaymentMethodType.CASH, null);
        when(paymentMethodService.getById(method.id())).thenReturn(method);

        mockMvc.perform(get(PaymentMethodController.BASE_PATH + "/{id}", method.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cash"));
    }

    @Test
    void shouldUpdatePaymentMethod() throws Exception {
        final UUID id = UUID.randomUUID();
        when(paymentMethodService.update(eq(id), eq("Card"), eq(PaymentMethodType.CARD), eq("iban")))
                .thenReturn(new PaymentMethod(id, "Card", PaymentMethodType.CARD, "iban"));

        mockMvc.perform(put(PaymentMethodController.BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Card",
                                    "type": "CARD",
                                    "configuration": "iban"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configuration").value("iban"));
    }

    @Test
    void shouldDeletePaymentMethod() throws Exception {
        final UUID id = UUID.randomUUID();
        doNothing().when(paymentMethodService).delete(id);

        mockMvc.perform(delete(PaymentMethodController.BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        final UUID id = UUID.randomUUID();
        when(paymentMethodService.getById(id)).thenThrow(new NotFoundException("paymentMethod", id));

        mockMvc.perform(get(PaymentMethodController.BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("paymentMethod with id " + id + " was not found"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post(PaymentMethodController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "type": "CASH"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
