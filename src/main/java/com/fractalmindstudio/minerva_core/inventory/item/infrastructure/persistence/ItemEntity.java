package com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence.ArticleEntity;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.TaxEntity;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence.LocationEntity;
import com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence.ProviderEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter
@Setter
public class ItemEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)
    private ArticleEntity article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus itemStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id", referencedColumnName = "id")
    private ItemEntity parentItem;

    @Column(nullable = false)
    private boolean hasChildren;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_tax_id", referencedColumnName = "id")
    private TaxEntity buyTax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "special_buy_tax_id", referencedColumnName = "id")
    private TaxEntity specialBuyTax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", referencedColumnName = "id")
    private ProviderEntity provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private LocationEntity location;

    @Column(name = "origin_purchase_id", length = 36)
    private String originPurchaseId;
}
