package com.fractalmindstudio.minerva_core.catalog.article.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository {

    Article save(Article article);

    Optional<Article> findById(UUID id);

    List<Article> findAll();

    List<Article> findByParentArticleId(UUID parentArticleId);

    void deleteById(UUID id);
}
