package com.fractalmindstudio.minerva_core.inventory.item.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ItemService {

    public static final String RESOURCE_NAME = "item";

    private final ItemRepository itemRepository;
    private final ArticleRepository articleRepository;

    public ItemService(final ItemRepository itemRepository, final ArticleRepository articleRepository) {
        this.itemRepository = itemRepository;
        this.articleRepository = articleRepository;
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
        final Item parentItem = itemRepository.save(Item.create(
                articleId, itemStatus, parentItemId, hasChildren,
                cost, buyTaxId, specialBuyTaxId, providerId, locationId
        ));

        if (hasChildren && itemStatus == ItemStatus.OPENED) {
            createChildItems(parentItem);
        }

        return parentItem;
    }

    private void createChildItems(final Item parentItem) {
        final Article parentArticle = articleRepository.findById(parentItem.articleId())
                .orElseThrow(() -> new NotFoundException("article", parentItem.articleId()));

        final List<Article> childArticles = articleRepository.findByParentArticleId(parentArticle.id());
        if (childArticles.isEmpty()) {
            return;
        }

        final Article childArticle = childArticles.getFirst();
        final int numberOfChildren = parentArticle.numberOfChildren();
        final BigDecimal childCost = parentItem.cost()
                .divide(BigDecimal.valueOf(numberOfChildren), 2, RoundingMode.HALF_UP);

        for (int i = 0; i < numberOfChildren; i++) {
            itemRepository.save(Item.create(
                    childArticle.id(),
                    ItemStatus.AVAILABLE,
                    parentItem.id(),
                    false,
                    childCost,
                    parentItem.buyTaxId(),
                    parentItem.specialBuyTaxId(),
                    parentItem.providerId(),
                    parentItem.locationId()
            ));
        }
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
