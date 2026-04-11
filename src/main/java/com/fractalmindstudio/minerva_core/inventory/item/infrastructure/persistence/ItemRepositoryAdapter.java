package com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence.SpringDataArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence.SpringDataLocationRepository;
import com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence.SpringDataProviderRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
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
        return springDataItemRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Item> findAllByOriginPurchaseId(final UUID originPurchaseId) {
        return springDataItemRepository.findAllByOriginPurchaseIdOrderByIdAsc(UuidMapper.toString(originPurchaseId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataItemRepository.deleteById(UuidMapper.toString(id));
    }

    @Override
    public void deleteAllByOriginPurchaseId(final UUID originPurchaseId) {
        springDataItemRepository.deleteAllByOriginPurchaseId(UuidMapper.toString(originPurchaseId));
    }

    private ItemEntity toEntity(final Item item) {
        final ItemEntity entity = new ItemEntity();
        entity.setId(UuidMapper.toString(item.id()));
        entity.setArticle(resolveReference(springDataArticleRepository, item.articleId(), "article"));
        entity.setItemStatus(item.itemStatus());
        entity.setParentItem(resolveReference(springDataItemRepository, item.parentItemId(), "item"));
        entity.setHasChildren(item.hasChildren());
        entity.setCost(item.cost());
        entity.setBuyTax(resolveReference(springDataTaxRepository, item.buyTaxId(), "tax"));
        entity.setSpecialBuyTax(resolveReference(springDataTaxRepository, item.specialBuyTaxId(), "tax"));
        entity.setProvider(resolveReference(springDataProviderRepository, item.providerId(), "provider"));
        entity.setLocation(resolveReference(springDataLocationRepository, item.locationId(), "location"));
        entity.setOriginPurchaseId(UuidMapper.toString(item.originPurchaseId()));
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
                entity.getLocation() != null ? UuidMapper.fromString(entity.getLocation().getId()) : null,
                UuidMapper.fromString(entity.getOriginPurchaseId())
        );
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
