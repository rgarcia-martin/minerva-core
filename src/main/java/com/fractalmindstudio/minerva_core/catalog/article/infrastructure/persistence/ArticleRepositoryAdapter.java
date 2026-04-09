package com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ArticleRepositoryAdapter implements ArticleRepository {

    private final SpringDataArticleRepository springDataArticleRepository;

    public ArticleRepositoryAdapter(final SpringDataArticleRepository springDataArticleRepository) {
        this.springDataArticleRepository = springDataArticleRepository;
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
        entity.setTaxId(UuidMapper.toString(article.taxId()));
        entity.setBasePrice(article.basePrice());
        entity.setRetailPrice(article.retailPrice());
        entity.setCanHaveChildren(article.canHaveChildren());
        entity.setNumberOfChildren(article.numberOfChildren());
        entity.setParentArticleId(UuidMapper.toString(article.parentArticleId()));
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
                UuidMapper.fromString(entity.getTaxId()),
                entity.getBasePrice(),
                entity.getRetailPrice(),
                entity.isCanHaveChildren(),
                entity.getNumberOfChildren(),
                UuidMapper.fromString(entity.getParentArticleId())
        );
    }
}
