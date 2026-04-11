package com.fractalmindstudio.minerva_core.purchasing.purchase.domain;

import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseLine(
        UUID id,
        UUID articleId,
        UUID itemId,
        int quantity,
        BigDecimal buyPrice,
        BigDecimal profitMargin,
        UUID taxId,
        ItemStatus itemStatus,
        boolean hasChildren
) {

    public static final String FIELD_ID = "purchaseLine.id";
    public static final String FIELD_ARTICLE_ID = "purchaseLine.articleId";
    public static final String FIELD_QUANTITY = "purchaseLine.quantity";
    public static final String FIELD_BUY_PRICE = "purchaseLine.buyPrice";
    public static final String FIELD_PROFIT_MARGIN = "purchaseLine.profitMargin";
    public static final String FIELD_TAX_ID = "purchaseLine.taxId";

    public PurchaseLine {
        DomainRules.requireNonNull(id, FIELD_ID);
        DomainRules.requireNonNull(articleId, FIELD_ARTICLE_ID);
        if (quantity <= 0) {
            throw new IllegalArgumentException(FIELD_QUANTITY + " must be greater than zero");
        }
        DomainRules.requirePositiveOrZero(buyPrice, FIELD_BUY_PRICE);
        buyPrice = DomainRules.scaleMoney(buyPrice);
        DomainRules.requirePositiveOrZero(profitMargin, FIELD_PROFIT_MARGIN);
        profitMargin = DomainRules.scaleRate(profitMargin);
        DomainRules.requireNonNull(taxId, FIELD_TAX_ID);
        itemStatus = itemStatus == null ? ItemStatus.AVAILABLE : itemStatus;
    }

    public static PurchaseLine create(
            final UUID articleId,
            final int quantity,
            final BigDecimal buyPrice,
            final BigDecimal profitMargin,
            final UUID taxId
    ) {
        return create(articleId, quantity, buyPrice, profitMargin, taxId, null, false);
    }

    public static PurchaseLine create(
            final UUID articleId,
            final int quantity,
            final BigDecimal buyPrice,
            final BigDecimal profitMargin,
            final UUID taxId,
            final ItemStatus itemStatus,
            final boolean hasChildren
    ) {
        return new PurchaseLine(
                UUID.randomUUID(),
                articleId,
                null,
                quantity,
                buyPrice,
                profitMargin,
                taxId,
                itemStatus,
                hasChildren
        );
    }

    public BigDecimal lineTotal() {
        return DomainRules.scaleMoney(buyPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
