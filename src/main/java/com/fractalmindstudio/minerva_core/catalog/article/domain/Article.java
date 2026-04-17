package com.fractalmindstudio.minerva_core.catalog.article.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Article(
        UUID id,
        String name,
        String code,
        String barcode,
        String image,
        String description,
        UUID taxId,
        BigDecimal basePrice,
        BigDecimal retailPrice,
        List<ArticleChild> children
) {

    public static final String FIELD_ID = "article.id";
    public static final String FIELD_NAME = "article.name";
    public static final String FIELD_CODE = "article.code";
    public static final String FIELD_TAX_ID = "article.taxId";
    public static final String FIELD_BASE_PRICE = "article.basePrice";
    public static final String FIELD_RETAIL_PRICE = "article.retailPrice";
    public static final String FIELD_CHILDREN = "article.children";

    public Article {
        DomainRules.requireNonNull(id, FIELD_ID);
        name = DomainRules.requireNonBlank(name, FIELD_NAME);
        code = DomainRules.requireNonBlank(code, FIELD_CODE);
        barcode = DomainRules.trimToNull(barcode);
        image = DomainRules.trimToNull(image);
        description = DomainRules.trimToNull(description);
        DomainRules.requireNonNull(taxId, FIELD_TAX_ID);
        DomainRules.requirePositiveOrZero(basePrice, FIELD_BASE_PRICE);
        DomainRules.requirePositiveOrZero(retailPrice, FIELD_RETAIL_PRICE);
        basePrice = DomainRules.scaleMoney(basePrice);
        retailPrice = DomainRules.scaleMoney(retailPrice);

        children = children == null ? List.of() : List.copyOf(children);

        if (children.stream().anyMatch(c -> id.equals(c.childArticleId()))) {
            throw new IllegalArgumentException(FIELD_CHILDREN + " must not reference the article itself");
        }

        final long distinctChildIds = children.stream()
                .map(ArticleChild::childArticleId)
                .distinct()
                .count();
        if (distinctChildIds != children.size()) {
            throw new IllegalArgumentException(FIELD_CHILDREN + " must not contain duplicate child article references");
        }
    }

    /** Derived convenience — true when this article has at least one child definition. */
    public boolean canHaveChildren() {
        return !children.isEmpty();
    }

    public static Article create(
            final String name,
            final String code,
            final String barcode,
            final String image,
            final String description,
            final UUID taxId,
            final BigDecimal basePrice,
            final BigDecimal retailPrice,
            final List<ArticleChild> children
    ) {
        return new Article(
                UUID.randomUUID(),
                name,
                code,
                barcode,
                image,
                description,
                taxId,
                basePrice,
                retailPrice,
                children
        );
    }
}
