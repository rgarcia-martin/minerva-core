package com.fractalmindstudio.minerva_core.catalog.article.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArticleChildTest {

    @Test
    void shouldCreateValidArticleChild() {
        final UUID childId = UUID.randomUUID();

        final ArticleChild child = new ArticleChild(childId, 5);

        assertEquals(childId, child.childArticleId());
        assertEquals(5, child.quantity());
    }

    @Test
    void shouldRejectNullChildArticleId() {
        assertThrows(NullPointerException.class, () -> new ArticleChild(null, 3));
    }

    @Test
    void shouldRejectZeroQuantity() {
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> new ArticleChild(UUID.randomUUID(), 0));
        assertNotNull(ex.getMessage());
    }

    @Test
    void shouldRejectNegativeQuantity() {
        final var ex = assertThrows(IllegalArgumentException.class,
                () -> new ArticleChild(UUID.randomUUID(), -1));
        assertNotNull(ex.getMessage());
    }
}
