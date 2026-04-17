package com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleChild;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ArticleRepositoryAdapter implements ArticleRepository {

    private final SpringDataArticleRepository springDataArticleRepository;
    private final SpringDataTaxRepository springDataTaxRepository;

    public ArticleRepositoryAdapter(
            final SpringDataArticleRepository springDataArticleRepository,
            final SpringDataTaxRepository springDataTaxRepository
    ) {
        this.springDataArticleRepository = springDataArticleRepository;
        this.springDataTaxRepository = springDataTaxRepository;
    }

    @Override
    public Article save(final Article article) {
        return toDomain(springDataArticleRepository.save(toEntity(article)));
    }

    @Override
    public Optional<Article> findById(final UUID id) {
        return springDataArticleRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<Article> findAll() {
        return springDataArticleRepository.findAllByOrderByNameAscCodeAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataArticleRepository.deleteById(UuidMapper.toString(id));
    }

    private ArticleEntity toEntity(final Article article) {
        final ArticleEntity entity = new ArticleEntity();
        entity.setId(UuidMapper.toString(article.id()));
        entity.setName(article.name());
        entity.setCode(article.code());
        entity.setBarcode(article.barcode());
        entity.setImage(article.image());
        entity.setDescription(article.description());
        entity.setTax(resolveReference(springDataTaxRepository, article.taxId(), "tax"));
        entity.setBasePrice(article.basePrice());
        entity.setRetailPrice(article.retailPrice());
        entity.setChildren(toChildEntities(article.children()));
        return entity;
    }

    private List<ArticleChildEntity> toChildEntities(final List<ArticleChild> children) {
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(children.stream().map(this::toChildEntity).toList());
    }

    private ArticleChildEntity toChildEntity(final ArticleChild child) {
        final ArticleChildEntity entity = new ArticleChildEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setChildArticle(resolveReference(springDataArticleRepository, child.childArticleId(), "article"));
        entity.setQuantity(child.quantity());
        return entity;
    }

    private Article toDomain(final ArticleEntity entity) {
        return new Article(
                UuidMapper.fromString(entity.getId()),
                entity.getName(),
                entity.getCode(),
                entity.getBarcode(),
                entity.getImage(),
                entity.getDescription(),
                entity.getTax() != null ? UuidMapper.fromString(entity.getTax().getId()) : null,
                entity.getBasePrice(),
                entity.getRetailPrice(),
                toChildDomains(entity.getChildren())
        );
    }

    private List<ArticleChild> toChildDomains(final List<ArticleChildEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream()
                .map(ce -> new ArticleChild(
                        UuidMapper.fromString(ce.getChildArticle().getId()),
                        ce.getQuantity()
                ))
                .toList();
    }

    private static <T> T resolveReference(
            final JpaRepository<T, String> repository,
            final UUID id,
            final String resourceName
    ) {
        if (id == null) {
            return null;
        }
        return repository.findById(UuidMapper.toString(id))
                .orElseThrow(() -> new NotFoundException(resourceName, id));
    }
}
