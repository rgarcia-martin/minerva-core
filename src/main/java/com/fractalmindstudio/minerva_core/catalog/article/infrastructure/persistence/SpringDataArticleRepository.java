package com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataArticleRepository extends JpaRepository<ArticleEntity, String> {

    List<ArticleEntity> findByParentArticle_Id(String parentArticleId);
}
