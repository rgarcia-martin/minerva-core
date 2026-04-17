package com.fractalmindstudio.minerva_core.catalog.article.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleTest {

    private static final String VALID_NAME = "Gaming Laptop";
    private static final String VALID_CODE = "LAP-001";
    private static final String VALID_BARCODE = "8400000012345";
    private static final String VALID_IMAGE = "https://cdn.example.com/laptop.png";
    private static final String VALID_DESCRIPTION = "14-inch performance laptop";
    private static final BigDecimal VALID_BASE_PRICE = new BigDecimal("1000.00");
    private static final BigDecimal VALID_RETAIL_PRICE = new BigDecimal("1210.00");

    @Test
    void shouldCreateArticleWithGeneratedId() {
        final UUID taxId = UUID.randomUUID();

        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, VALID_IMAGE, VALID_DESCRIPTION,
                taxId, VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        );

        assertNotNull(article.id());
        assertEquals(VALID_NAME, article.name());
        assertEquals(VALID_CODE, article.code());
        assertEquals(VALID_BARCODE, article.barcode());
        assertEquals(taxId, article.taxId());
    }

    @Test
    void shouldScaleBasePriceToTwoDecimalPlaces() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), new BigDecimal("1000.125"), VALID_RETAIL_PRICE, List.of()
        );

        assertEquals(new BigDecimal("1000.13"), article.basePrice());
    }

    @Test
    void shouldScaleRetailPriceToTwoDecimalPlaces() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, new BigDecimal("1210.556"), List.of()
        );

        assertEquals(new BigDecimal("1210.56"), article.retailPrice());
    }

    @Test
    void shouldCreateArticleWithMultipleChildren() {
        final UUID childA = UUID.randomUUID();
        final UUID childB = UUID.randomUUID();

        final Article article = Article.create(
                "Combo Pack", "COMBO-001", null, null, "Pack with two products",
                UUID.randomUUID(), new BigDecimal("20.00"), new BigDecimal("30.00"),
                List.of(new ArticleChild(childA, 5), new ArticleChild(childB, 3))
        );

        assertTrue(article.canHaveChildren());
        assertEquals(2, article.children().size());
        assertEquals(childA, article.children().get(0).childArticleId());
        assertEquals(5, article.children().get(0).quantity());
        assertEquals(childB, article.children().get(1).childArticleId());
        assertEquals(3, article.children().get(1).quantity());
    }

    @Test
    void shouldCreateArticleWithSingleChild() {
        final UUID childId = UUID.randomUUID();

        final Article article = Article.create(
                "Box of Pens", "BOX-PEN-001", "8400000099999", null, "Box containing 12 pens",
                UUID.randomUUID(), new BigDecimal("12.00"), new BigDecimal("18.00"),
                List.of(new ArticleChild(childId, 12))
        );

        assertTrue(article.canHaveChildren());
        assertEquals(1, article.children().size());
        assertEquals(childId, article.children().get(0).childArticleId());
        assertEquals(12, article.children().get(0).quantity());
    }

    @Test
    void shouldCreateStandaloneArticleWithEmptyChildren() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        );

        assertFalse(article.canHaveChildren());
        assertTrue(article.children().isEmpty());
    }

    @Test
    void shouldCreateArticleWithNullChildrenDefaultingToEmpty() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE, null
        );

        assertFalse(article.canHaveChildren());
        assertTrue(article.children().isEmpty());
    }

    @Test
    void shouldMakeChildrenListImmutable() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                List.of(new ArticleChild(UUID.randomUUID(), 2))
        );

        assertThrows(UnsupportedOperationException.class,
                () -> article.children().add(new ArticleChild(UUID.randomUUID(), 1)));
    }

    @Test
    void shouldRejectSelfReferenceInChildren() {
        final UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> new Article(
                id, VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                List.of(new ArticleChild(id, 2))
        ));
    }

    @Test
    void shouldRejectDuplicateChildArticleIds() {
        final UUID childId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                List.of(new ArticleChild(childId, 2), new ArticleChild(childId, 5))
        ));
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new Article(
                null, VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        ));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                "", VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        ));
    }

    @Test
    void shouldRejectBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, "", VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        ));
    }

    @Test
    void shouldRejectNullTaxId() {
        assertThrows(NullPointerException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                null, VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        ));
    }

    @Test
    void shouldRejectNegativeBasePrice() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), new BigDecimal("-0.01"), VALID_RETAIL_PRICE, List.of()
        ));
    }

    @Test
    void shouldRejectNegativeRetailPrice() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, new BigDecimal("-0.01"), List.of()
        ));
    }

    @Test
    void shouldTrimAndNullifyOptionalFields() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, "  ", "  ", "  ",
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        );

        assertNull(article.barcode());
        assertNull(article.image());
        assertNull(article.description());
    }

    @Test
    void shouldTrimNameAndCode() {
        final Article article = Article.create(
                "  Laptop  ", "  LAP-001  ", VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE, List.of()
        );

        assertEquals("Laptop", article.name());
        assertEquals("LAP-001", article.code());
    }
}
