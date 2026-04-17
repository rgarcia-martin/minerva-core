package com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "article_children")
@Getter
@Setter
public class ArticleChildEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_article_id", referencedColumnName = "id", nullable = false)
    private ArticleEntity childArticle;

    @Column(nullable = false)
    private int quantity;
}
