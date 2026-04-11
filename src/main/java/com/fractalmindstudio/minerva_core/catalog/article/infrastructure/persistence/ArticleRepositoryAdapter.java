package com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

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
        return springDataArticleRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Article> findByParentArticleId(final UUID parentArticleId) {
        return springDataArticleRepository.findByParentArticle_Id(UuidMapper.toString(parentArticleId))
                .stream().map(this::toDomain).toList();
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
        entity.setTax(resolveReference(springDataTaxRepository, UuidMapper.toString(article.taxId())));
        entity.setBasePrice(article.basePrice());
        entity.setRetailPrice(article.retailPrice());
        entity.setCanHaveChildren(article.canHaveChildren());
        entity.setNumberOfChildren(article.numberOfChildren());
        entity.setParentArticle(resolveReference(springDataArticleRepository, UuidMapper.toString(article.parentArticleId())));
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
                entity.isCanHaveChildren(),
                entity.getNumberOfChildren(),
                entity.getParentArticle() != null ? UuidMapper.fromString(entity.getParentArticle().getId()) : null
        );
    }

    private static <T> T resolveReference(
            final org.springframework.data.jpa.repository.JpaRepository<T, String> repository,
            final String id
    ) {
        return id == null ? null : repository.getReferenceById(id);
    }
}
