package com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence.SpringDataArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence.SpringDataLocationRepository;
import com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence.SpringDataProviderRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ItemRepositoryAdapter implements ItemRepository {

    private final SpringDataItemRepository springDataItemRepository;
    private final SpringDataArticleRepository springDataArticleRepository;
    private final SpringDataTaxRepository springDataTaxRepository;
    private final SpringDataProviderRepository springDataProviderRepository;
    private final SpringDataLocationRepository springDataLocationRepository;

    public ItemRepositoryAdapter(
            final SpringDataItemRepository springDataItemRepository,
            final SpringDataArticleRepository springDataArticleRepository,
            final SpringDataTaxRepository springDataTaxRepository,
            final SpringDataProviderRepository springDataProviderRepository,
            final SpringDataLocationRepository springDataLocationRepository
    ) {
        this.springDataItemRepository = springDataItemRepository;
        this.springDataArticleRepository = springDataArticleRepository;
        this.springDataTaxRepository = springDataTaxRepository;
        this.springDataProviderRepository = springDataProviderRepository;
        this.springDataLocationRepository = springDataLocationRepository;
    }

    @Override
    public Item save(final Item item) {
        return toDomain(springDataItemRepository.save(toEntity(item)));
    }

    @Override
    public Optional<Item> findById(final UUID id) {
        return springDataItemRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<Item> findAll() {
        return springDataItemRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataItemRepository.deleteById(UuidMapper.toString(id));
    }

    private ItemEntity toEntity(final Item item) {
        final ItemEntity entity = new ItemEntity();
        entity.setId(UuidMapper.toString(item.id()));
        entity.setArticle(resolveReference(springDataArticleRepository, UuidMapper.toString(item.articleId())));
        entity.setItemStatus(item.itemStatus());
        entity.setParentItem(resolveReference(springDataItemRepository, UuidMapper.toString(item.parentItemId())));
        entity.setHasChildren(item.hasChildren());
        entity.setCost(item.cost());
        entity.setBuyTax(resolveReference(springDataTaxRepository, UuidMapper.toString(item.buyTaxId())));
        entity.setSpecialBuyTax(resolveReference(springDataTaxRepository, UuidMapper.toString(item.specialBuyTaxId())));
        entity.setProvider(resolveReference(springDataProviderRepository, UuidMapper.toString(item.providerId())));
        entity.setLocation(resolveReference(springDataLocationRepository, UuidMapper.toString(item.locationId())));
        return entity;
    }

    private Item toDomain(final ItemEntity entity) {
        return new Item(
                UuidMapper.fromString(entity.getId()),
                entity.getArticle() != null ? UuidMapper.fromString(entity.getArticle().getId()) : null,
                entity.getItemStatus(),
                entity.getParentItem() != null ? UuidMapper.fromString(entity.getParentItem().getId()) : null,
                entity.isHasChildren(),
                entity.getCost(),
                entity.getBuyTax() != null ? UuidMapper.fromString(entity.getBuyTax().getId()) : null,
                entity.getSpecialBuyTax() != null ? UuidMapper.fromString(entity.getSpecialBuyTax().getId()) : null,
                entity.getProvider() != null ? UuidMapper.fromString(entity.getProvider().getId()) : null,
                entity.getLocation() != null ? UuidMapper.fromString(entity.getLocation().getId()) : null
        );
    }

    private static <T> T resolveReference(final JpaRepository<T, String> repository, final String id) {
        return id == null ? null : repository.getReferenceById(id);
    }
}
