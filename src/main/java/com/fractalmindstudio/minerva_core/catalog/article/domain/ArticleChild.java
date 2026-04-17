package com.fractalmindstudio.minerva_core.catalog.article.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.util.UUID;

/**
 * Value object representing the relationship between a parent article and one of its children.
 * Carries the quantity metadata (how many units of the child the parent contains).
 */
public record ArticleChild(UUID childArticleId, int quantity) {

    public static final String FIELD_CHILD_ARTICLE_ID = "articleChild.childArticleId";
    public static final String FIELD_QUANTITY = "articleChild.quantity";

    public ArticleChild {
        DomainRules.requireNonNull(childArticleId, FIELD_CHILD_ARTICLE_ID);
        if (quantity <= 0) {
            throw new IllegalArgumentException(FIELD_QUANTITY + " must be greater than zero");
        }
    }
}
