package com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.TaxEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
@Setter
@Getter
public class ArticleEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(unique = true)
    private String barcode;

    @Column(length = 4000)
    private String image;

    @Column(length = 4000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_id", referencedColumnName = "id", nullable = false)
    private TaxEntity tax;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal retailPrice;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_article_id", nullable = false)
    private List<ArticleChildEntity> children = new ArrayList<>();
}
