package com.fractalmindstudio.minerva_core.inventory.item.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.util.UUID;

public record Item(
        UUID id,
        UUID articleId,
        ItemStatus itemStatus,
        UUID parentItemId,
        boolean hasChildren,
        BigDecimal cost,
        UUID buyTaxId,
        UUID specialBuyTaxId,
        UUID providerId,
        UUID locationId
) {

    public static final String FIELD_ID = "item.id";
    public static final String FIELD_ARTICLE_ID = "item.articleId";
    public static final String FIELD_COST = "item.cost";

    public Item {
        DomainRules.requireNonNull(id, FIELD_ID);
        DomainRules.requireNonNull(articleId, FIELD_ARTICLE_ID);
        itemStatus = itemStatus == null ? ItemStatus.AVAILABLE : itemStatus;
        DomainRules.requirePositiveOrZero(cost, FIELD_COST);
        cost = DomainRules.scaleMoney(cost);
    }

    public static Item create(
            final UUID articleId,
            final ItemStatus itemStatus,
            final UUID parentItemId,
            final boolean hasChildren,
            final BigDecimal cost,
            final UUID buyTaxId,
            final UUID specialBuyTaxId,
            final UUID providerId,
            final UUID locationId
    ) {
        return new Item(
                UUID.randomUUID(),
                articleId,
                itemStatus,
                parentItemId,
                hasChildren,
                cost,
                buyTaxId,
                specialBuyTaxId,
                providerId,
                locationId
        );
    }

    public Item markAsSold() {
        return new Item(id, articleId, ItemStatus.SOLD, parentItemId, hasChildren, cost, buyTaxId, specialBuyTaxId, providerId, locationId);
    }

    public Item markAsAvailable() {
        return new Item(id, articleId, ItemStatus.AVAILABLE, parentItemId, hasChildren, cost, buyTaxId, specialBuyTaxId, providerId, locationId);
    }
}
