package com.fractalmindstudio.minerva_core.catalog.article.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
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
        boolean canHaveChildren,
        int numberOfChildren,
        UUID parentArticleId
) {

    public static final String FIELD_ID = "article.id";
    public static final String FIELD_NAME = "article.name";
    public static final String FIELD_CODE = "article.code";
    public static final String FIELD_BARCODE = "article.barcode";
    public static final String FIELD_TAX_ID = "article.taxId";
    public static final String FIELD_BASE_PRICE = "article.basePrice";
    public static final String FIELD_RETAIL_PRICE = "article.retailPrice";
    public static final String FIELD_NUMBER_OF_CHILDREN = "article.numberOfChildren";

    public Article {
        DomainRules.requireNonNull(id, FIELD_ID);
        name = DomainRules.requireNonBlank(name, FIELD_NAME);
        code = DomainRules.requireNonBlank(code, FIELD_CODE);
        DomainRules.requireNonNull(taxId, FIELD_TAX_ID);
        DomainRules.requirePositiveOrZero(basePrice, FIELD_BASE_PRICE);
        DomainRules.requirePositiveOrZero(retailPrice, FIELD_RETAIL_PRICE);
        basePrice = DomainRules.scaleMoney(basePrice);
        retailPrice = DomainRules.scaleMoney(retailPrice);
        DomainRules.requirePositiveOrZero(numberOfChildren, FIELD_NUMBER_OF_CHILDREN);
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
            final boolean canHaveChildren,
            final int numberOfChildren,
            final UUID parentArticleId
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
                canHaveChildren,
                numberOfChildren,
                parentArticleId
        );
    }

}
