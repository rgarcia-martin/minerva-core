package com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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

    @Column(nullable = false, length = 36)
    private String articleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus itemStatus;

    @Column(length = 36)
    private String parentItemId;

    @Column(nullable = false)
    private boolean hasChildren;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cost;

    @Column(length = 36)
    private String buyTaxId;

    @Column(length = 36)
    private String specialBuyTaxId;

    @Column(length = 36)
    private String providerId;

    @Column(length = 36)
    private String locationId;
}
