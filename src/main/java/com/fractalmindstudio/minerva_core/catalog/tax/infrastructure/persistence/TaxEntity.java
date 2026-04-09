package com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "taxes")
@Getter
@Setter
public class TaxEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal surchargeRate;
}
