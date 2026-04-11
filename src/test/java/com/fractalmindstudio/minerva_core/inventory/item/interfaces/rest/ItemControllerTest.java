package com.fractalmindstudio.minerva_core.inventory.item.interfaces.rest;

import com.fractalmindstudio.minerva_core.inventory.item.application.ItemService;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private static final String BASE_PATH = ItemController.BASE_PATH;
    private static final UUID ARTICLE_ID = UUID.randomUUID();

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldFindAllItems() throws Exception {
        final var items = List.of(
                Item.create(ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null)
        );
        when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].articleId").value(ARTICLE_ID.toString()));
    }

    @Test
    void shouldGetItemById() throws Exception {
        final var item = Item.create(ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null);
        when(itemService.getById(item.id())).thenReturn(item);

        mockMvc.perform(get(BASE_PATH + "/{id}", item.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(ARTICLE_ID.toString()))
                .andExpect(jsonPath("$.itemStatus").value("AVAILABLE"));
    }

    @Test
    void shouldReturn404WhenItemNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(itemService.getById(id)).thenThrow(new NotFoundException("item", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("item with id " + id + " was not found"));
    }

    @Test
    void shouldReturn400WhenPathVariableIsInvalidUuid() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/{id}", "not-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn405WhenCreateIsAttempted() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void shouldReturn405WhenUpdateIsAttempted() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void shouldReturn405WhenDeleteIsAttempted() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/{id}", UUID.randomUUID()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(itemService.findAll()).thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected application error"));
    }
}
