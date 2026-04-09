package com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataArticleRepository extends JpaRepository<ArticleEntity, String> {
}
