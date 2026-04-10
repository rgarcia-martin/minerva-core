package com.fractalmindstudio.minerva_core.purchasing.purchase.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence.ArticleEntity;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.TaxEntity;
import com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence.ItemEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_lines")
@Getter
@Setter
public class PurchaseLineEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)
    private ArticleEntity article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private ItemEntity item;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal buyPrice;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal profitMargin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_id", referencedColumnName = "id", nullable = false)
    private TaxEntity tax;
}
