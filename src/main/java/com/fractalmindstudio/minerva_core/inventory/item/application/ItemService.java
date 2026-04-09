package com.fractalmindstudio.minerva_core.inventory.item.application;

import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ItemService {

    public static final String RESOURCE_NAME = "item";

    private final ItemRepository itemRepository;

    public ItemService(final ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Item create(
            final UUID articleId,
            final ItemStatus itemStatus,
            final UUID parentItemId,
            final boolean hasChildren,
            final BigDecimal cost,
            final UUID buyTaxId,
            final UUID specialBuyTaxId,
            final UUID providerId,
            final UUID locationId
    ) {
        return itemRepository.save(Item.create(
                articleId,
                itemStatus,
                parentItemId,
                hasChildren,
                cost,
                buyTaxId,
                specialBuyTaxId,
                providerId,
                locationId
        ));
    }

    public Item getById(final UUID id) {
        return itemRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Item> findAll() {
        return itemRepository.findAll().stream()
                .sorted(Comparator.comparing(Item::id))
                .toList();
    }

    @Transactional
    public Item update(
            final UUID id,
            final UUID articleId,
            final ItemStatus itemStatus,
            final UUID parentItemId,
            final boolean hasChildren,
            final BigDecimal cost,
            final UUID buyTaxId,
            final UUID specialBuyTaxId,
            final UUID providerId,
            final UUID locationId
    ) {
        getById(id);
        return itemRepository.save(new Item(
                id,
                articleId,
                itemStatus,
                parentItemId,
                hasChildren,
                cost,
                buyTaxId,
                specialBuyTaxId,
                providerId,
                locationId
        ));
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        itemRepository.deleteById(id);
    }
}
