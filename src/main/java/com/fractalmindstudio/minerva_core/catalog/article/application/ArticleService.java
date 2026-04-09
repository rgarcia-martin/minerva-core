package com.fractalmindstudio.minerva_core.catalog.article.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@Log4j2
public class ArticleService {

    public static final String RESOURCE_NAME = "article";

    private final ArticleRepository articleRepository;

    public ArticleService(final ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional
    public Article create(
            final String name,
            final String code,
            final String barcode,
            final String image,
            final String description,
            final UUID taxId,
            final BigDecimal basePrice,
            final BigDecimal retailPrice,
            final boolean canHaveChildren,
            final int numberOfChildren,
            final UUID parentArticleId
    ) {
        log.debug("Creating new Article");
        return articleRepository.save(Article.create(
                name,
                code,
                barcode,
                image,
                description,
                taxId,
                basePrice,
                retailPrice,
                canHaveChildren,
                numberOfChildren,
                parentArticleId
        ));
    }

    public Article getById(final UUID id) {
        log.debug("getById {}", id);
        return articleRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Article> findAll() {
        log.debug("findAll");
        return articleRepository.findAll().stream()
                .sorted(Comparator.comparing(Article::name).thenComparing(Article::code))
                .toList();
    }

    @Transactional
    public Article update(
            final UUID id,
            final String name,
            final String code,
            final String barcode,
            final String image,
            final String description,
            final UUID taxId,
            final BigDecimal basePrice,
            final BigDecimal retailPrice,
            final boolean canHaveChildren,
            final int numberOfChildren,
            final UUID parentArticleId
    ) {
        log.debug("update article {}", id);
        getById(id);
        return articleRepository.save(new Article(
                id,
                name,
                code,
                barcode,
                image,
                description,
                taxId,
                basePrice,
                retailPrice,
                canHaveChildren,
                numberOfChildren,
                parentArticleId
        ));
    }

    @Transactional
    public void delete(final UUID id) {
        log.debug("delete article {}", id);
        getById(id);
        articleRepository.deleteById(id);
    }
}
