package com.fractalmindstudio.minerva_core.catalog.freeconcept.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.TaxEntity;
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
@Table(name = "free_concepts")
@Getter
@Setter
public class FreeConceptEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String barcode;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_id", referencedColumnName = "id")
    private TaxEntity tax;

    @Column(length = 4000)
    private String description;
}