package com.fractalmindstudio.minerva_core.catalog.article.interfaces.rest;

import com.fractalmindstudio.minerva_core.catalog.article.application.ArticleService;
import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleChild;
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
class ArticleControllerTest {

    private static final String BASE_PATH = ArticleController.BASE_PATH;
    private static final UUID TAX_ID = UUID.randomUUID();

    private MockMvc mockMvc;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private ArticleController articleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(articleController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private String validArticleJson() {
        return """
                {
                    "name": "Widget",
                    "code": "WDG-001",
                    "barcode": null,
                    "image": null,
                    "description": "A widget",
                    "taxId": "%s",
                    "basePrice": 10.00,
                    "retailPrice": 15.00
                }
                """.formatted(TAX_ID);
    }

    @Test
    void shouldCreateArticleWithoutChildren() throws Exception {
        final var article = Article.create(
                "Widget", "WDG-001", null, null, "A widget",
                TAX_ID, new BigDecimal("10"), new BigDecimal("15"), List.of()
        );
        when(articleService.create(
                eq("Widget"), eq("WDG-001"), isNull(), isNull(), eq("A widget"),
                eq(TAX_ID), any(BigDecimal.class), any(BigDecimal.class),
                eq(List.of())
        )).thenReturn(article);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validArticleJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(article.id().toString()))
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.canHaveChildren").value(false))
                .andExpect(jsonPath("$.children", hasSize(0)));
    }

    @Test
    void shouldCreateArticleWithChildren() throws Exception {
        final UUID childArticleId = UUID.randomUUID();
        final var children = List.of(new ArticleChild(childArticleId, 3));
        final var article = Article.create(
                "Pack", "PACK-001", null, null, null,
                TAX_ID, new BigDecimal("10"), new BigDecimal("15"), children
        );
        when(articleService.create(
                eq("Pack"), eq("PACK-001"), isNull(), isNull(), isNull(),
                eq(TAX_ID), any(BigDecimal.class), any(BigDecimal.class),
                eq(children)
        )).thenReturn(article);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Pack",
                                    "code": "PACK-001",
                                    "taxId": "%s",
                                    "basePrice": 10,
                                    "retailPrice": 15,
                                    "children": [
                                        {"childArticleId": "%s", "quantity": 3}
                                    ]
                                }
                                """.formatted(TAX_ID, childArticleId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.canHaveChildren").value(true))
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].childArticleId").value(childArticleId.toString()))
                .andExpect(jsonPath("$.children[0].quantity").value(3));
    }

    @Test
    void shouldFindAllArticles() throws Exception {
        when(articleService.findAll()).thenReturn(List.of(
                Article.create("A", "A-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of())
        ));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetArticleById() throws Exception {
        final var article = Article.create("Widget", "WDG-001", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of());
        when(articleService.getById(article.id())).thenReturn(article);

        mockMvc.perform(get(BASE_PATH + "/{id}", article.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    void shouldUpdateArticle() throws Exception {
        final var id = UUID.randomUUID();
        final var updated = new Article(id, "Updated", "UPD-001", null, null, "Updated desc",
                TAX_ID, new BigDecimal("20"), new BigDecimal("30"), List.of());
        when(articleService.update(
                eq(id), eq("Updated"), eq("UPD-001"), isNull(), isNull(), eq("Updated desc"),
                eq(TAX_ID), any(BigDecimal.class), any(BigDecimal.class),
                eq(List.of())
        )).thenReturn(updated);

        mockMvc.perform(put(BASE_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated", "code": "UPD-001", "barcode": null,
                                    "description": "Updated desc", "taxId": "%s",
                                    "basePrice": 20, "retailPrice": 30
                                }
                                """.formatted(TAX_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeleteArticle() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(articleService).delete(id);

        mockMvc.perform(delete(BASE_PATH + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenArticleNotFound() throws Exception {
        final var id = UUID.randomUUID();
        when(articleService.getById(id)).thenThrow(new NotFoundException("article", id));

        mockMvc.perform(get(BASE_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("article with id " + id + " was not found"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "", "code": "WDG-001",
                                    "taxId": "%s", "basePrice": 10, "retailPrice": 15
                                }
                                """.formatted(TAX_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenCodeIsMissing() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Widget",
                                    "taxId": "%s", "basePrice": 10, "retailPrice": 15
                                }
                                """.formatted(TAX_ID)))
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
}
