package com.fractalmindstudio.minerva_core.catalog.article.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleChild;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@Log4j2
public class ArticleService {

    public static final String RESOURCE_NAME = "article";
    public static final String TAX_RESOURCE_NAME = "tax";
    public static final String CYCLE_DETECTED_MESSAGE = "article children configuration contains a cycle";

    private final ArticleRepository articleRepository;
    private final TaxRepository taxRepository;

    public ArticleService(
            final ArticleRepository articleRepository,
            final TaxRepository taxRepository
    ) {
        this.articleRepository = articleRepository;
        this.taxRepository = taxRepository;
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
            final List<ArticleChild> children
    ) {
        log.debug("Creating new article");
        validateReferences(taxId, children);
        final Article article = Article.create(
                name, code, barcode, image, description,
                taxId, basePrice, retailPrice, children
        );
        validateNoCycles(article.id(), children);
        return articleRepository.save(article);
    }

    public Article getById(final UUID id) {
        log.debug("getById {}", id);
        return articleRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Article> findAll() {
        log.debug("findAll");
        return articleRepository.findAll();
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
            final List<ArticleChild> children
    ) {
        log.debug("update article {}", id);
        getById(id);
        validateReferences(taxId, children);
        validateNoCycles(id, children);
        return articleRepository.save(new Article(
                id, name, code, barcode, image, description,
                taxId, basePrice, retailPrice, children
        ));
    }

    @Transactional
    public void delete(final UUID id) {
        log.debug("delete article {}", id);
        getById(id);
        articleRepository.deleteById(id);
    }

    private void validateReferences(final UUID taxId, final List<ArticleChild> children) {
        if (taxRepository.findById(taxId).isEmpty()) {
            throw new NotFoundException(TAX_RESOURCE_NAME, taxId);
        }
        for (final ArticleChild child : children) {
            if (articleRepository.findById(child.childArticleId()).isEmpty()) {
                throw new NotFoundException(RESOURCE_NAME, child.childArticleId());
            }
        }
    }

    /**
     * BFS traversal to detect cycles in the article genealogical tree.
     * Starts from the given articleId and follows all descendant children references.
     * Throws if any descendant references back to the root articleId (cycle).
     * Diamond topologies (same node reachable via multiple paths) are allowed.
     */
    private void validateNoCycles(final UUID articleId, final List<ArticleChild> children) {
        final Set<UUID> visited = new HashSet<>();
        final Deque<UUID> queue = new ArrayDeque<>();
        for (final ArticleChild child : children) {
            queue.add(child.childArticleId());
        }
        while (!queue.isEmpty()) {
            final UUID current = queue.poll();
            if (current.equals(articleId)) {
                throw new IllegalArgumentException(CYCLE_DETECTED_MESSAGE);
            }
            if (!visited.add(current)) {
                continue;
            }
            final Article descendant = articleRepository.findById(current).orElse(null);
            if (descendant != null) {
                for (final ArticleChild grandchild : descendant.children()) {
                    queue.add(grandchild.childArticleId());
                }
            }
        }
    }
}
