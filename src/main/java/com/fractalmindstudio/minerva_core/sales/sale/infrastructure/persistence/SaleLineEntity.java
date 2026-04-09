package com.fractalmindstudio.minerva_core.sales.sale.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_lines")
@Getter
@Setter
public class SaleLineEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(length = 36)
    private String itemId;

    @Column(length = 36)
    private String freeConceptId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 36)
    private String taxId;
}
