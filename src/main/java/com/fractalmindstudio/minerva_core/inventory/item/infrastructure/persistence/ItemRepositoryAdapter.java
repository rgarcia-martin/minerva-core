package com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ItemRepositoryAdapter implements ItemRepository {

    private final SpringDataItemRepository springDataItemRepository;

    public ItemRepositoryAdapter(final SpringDataItemRepository springDataItemRepository) {
        this.springDataItemRepository = springDataItemRepository;
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
        entity.setArticleId(UuidMapper.toString(item.articleId()));
        entity.setItemStatus(item.itemStatus());
        entity.setParentItemId(UuidMapper.toString(item.parentItemId()));
        entity.setHasChildren(item.hasChildren());
        entity.setCost(item.cost());
        entity.setBuyTaxId(UuidMapper.toString(item.buyTaxId()));
        entity.setSpecialBuyTaxId(UuidMapper.toString(item.specialBuyTaxId()));
        entity.setProviderId(UuidMapper.toString(item.providerId()));
        entity.setLocationId(UuidMapper.toString(item.locationId()));
        return entity;
    }

    private Item toDomain(final ItemEntity entity) {
        return new Item(
                UuidMapper.fromString(entity.getId()),
                UuidMapper.fromString(entity.getArticleId()),
                entity.getItemStatus(),
                UuidMapper.fromString(entity.getParentItemId()),
                entity.isHasChildren(),
                entity.getCost(),
                UuidMapper.fromString(entity.getBuyTaxId()),
                UuidMapper.fromString(entity.getSpecialBuyTaxId()),
                UuidMapper.fromString(entity.getProviderId()),
                UuidMapper.fromString(entity.getLocationId())
        );
    }
}
