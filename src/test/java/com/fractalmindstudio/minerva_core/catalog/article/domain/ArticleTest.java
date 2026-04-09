package com.fractalmindstudio.minerva_core.catalog.article.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Article domain model.
 * An article represents a product in the catalog with pricing, tax linkage,
 * and optional parent-child relationship for packages (e.g., a box of pens
 * where each pen is sold individually).
 */
class ArticleTest {

    private static final String VALID_NAME = "Gaming Laptop";
    private static final String VALID_CODE = "LAP-001";
    private static final String VALID_BARCODE = "8400000012345";
    private static final String VALID_IMAGE = "https://cdn.example.com/laptop.png";
    private static final String VALID_DESCRIPTION = "14-inch performance laptop";
    private static final BigDecimal VALID_BASE_PRICE = new BigDecimal("1000.00");
    private static final BigDecimal VALID_RETAIL_PRICE = new BigDecimal("1210.00");
    private static final int ZERO_CHILDREN = 0;

    // --- Factory method ---

    @Test
    void shouldCreateArticleWithGeneratedId() {
        final UUID taxId = UUID.randomUUID();

        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, VALID_IMAGE, VALID_DESCRIPTION,
                taxId, VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        );

        assertNotNull(article.id());
        assertEquals(VALID_NAME, article.name());
        assertEquals(VALID_CODE, article.code());
        assertEquals(VALID_BARCODE, article.barcode());
        assertEquals(taxId, article.taxId());
    }

    // --- Price scaling ---

    @Test
    void shouldScaleBasePriceToTwoDecimalPlaces() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), new BigDecimal("1000.125"), VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        );

        assertEquals(new BigDecimal("1000.13"), article.basePrice());
    }

    @Test
    void shouldScaleRetailPriceToTwoDecimalPlaces() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, new BigDecimal("1210.556"),
                false, ZERO_CHILDREN, null
        );

        assertEquals(new BigDecimal("1210.56"), article.retailPrice());
    }

    // --- Package (parent-child) relationship ---

    @Test
    void shouldCreatePackageArticleWithChildReference() {
        final UUID childArticleId = UUID.randomUUID();

        final Article packageArticle = Article.create(
                "Box of Pens", "BOX-PEN-001", "8400000099999", null, "Box containing 12 pens",
                UUID.randomUUID(), new BigDecimal("12.00"), new BigDecimal("18.00"),
                true, 12, childArticleId
        );

        assertTrue(packageArticle.canHaveChildren());
        assertEquals(12, packageArticle.numberOfChildren());
        assertEquals(childArticleId, packageArticle.parentArticleId());
    }

    @Test
    void shouldCreateStandaloneArticleWithoutChildren() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        );

        assertFalse(article.canHaveChildren());
        assertEquals(ZERO_CHILDREN, article.numberOfChildren());
        assertNull(article.parentArticleId());
    }

    // --- Invariant enforcement ---

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new Article(
                null, VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        ));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                "", VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        ));
    }

    @Test
    void shouldRejectBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, "", VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        ));
    }

    @Test
    void shouldRejectNullTaxId() {
        assertThrows(NullPointerException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                null, VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        ));
    }

    @Test
    void shouldRejectNegativeBasePrice() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), new BigDecimal("-0.01"), VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        ));
    }

    @Test
    void shouldRejectNegativeRetailPrice() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, new BigDecimal("-0.01"),
                false, ZERO_CHILDREN, null
        ));
    }

    @Test
    void shouldRejectNegativeNumberOfChildren() {
        assertThrows(IllegalArgumentException.class, () -> Article.create(
                VALID_NAME, VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                true, -1, null
        ));
    }

    @Test
    void shouldTrimName() {
        final Article article = Article.create(
                "  Laptop  ", VALID_CODE, VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        );

        assertEquals("Laptop", article.name());
    }

    @Test
    void shouldTrimCode() {
        final Article article = Article.create(
                VALID_NAME, "  LAP-001  ", VALID_BARCODE, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        );

        assertEquals("LAP-001", article.code());
    }

    @Test
    void shouldAllowNullOptionalFields() {
        final Article article = Article.create(
                VALID_NAME, VALID_CODE, null, null, null,
                UUID.randomUUID(), VALID_BASE_PRICE, VALID_RETAIL_PRICE,
                false, ZERO_CHILDREN, null
        );

        assertNull(article.barcode());
        assertNull(article.image());
        assertNull(article.description());
    }
}
