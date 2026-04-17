package com.fractalmindstudio.minerva_core.purchasing.purchase.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleChild;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.inventory.location.domain.LocationRepository;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.ProviderRepository;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseRepository;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseState;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PurchaseService {

    public static final String RESOURCE_NAME = "purchase";
    public static final String PROVIDER_RESOURCE_NAME = "provider";
    public static final String LOCATION_RESOURCE_NAME = "location";
    public static final String ARTICLE_RESOURCE_NAME = "article";
    public static final String TAX_RESOURCE_NAME = "tax";

    private static final int MONEY_SCALE = 2;

    private final PurchaseRepository purchaseRepository;
    private final ProviderRepository providerRepository;
    private final LocationRepository locationRepository;
    private final ArticleRepository articleRepository;
    private final ItemRepository itemRepository;
    private final TaxRepository taxRepository;

    public PurchaseService(
            final PurchaseRepository purchaseRepository,
            final ProviderRepository providerRepository,
            final LocationRepository locationRepository,
            final ArticleRepository articleRepository,
            final ItemRepository itemRepository,
            final TaxRepository taxRepository
    ) {
        this.purchaseRepository = purchaseRepository;
        this.providerRepository = providerRepository;
        this.locationRepository = locationRepository;
        this.articleRepository = articleRepository;
        this.itemRepository = itemRepository;
        this.taxRepository = taxRepository;
    }

    @Transactional
    public Purchase create(
            final LocalDateTime createdOn,
            final LocalDateTime finishDate,
            final PurchaseState state,
            final String code,
            final String providerCode,
            final UUID providerId,
            final UUID locationId,
            final boolean deposit,
            final List<PurchaseLine> lines
    ) {
        validateReferences(providerId, locationId, lines);
        final Purchase purchase = purchaseRepository.save(Purchase.create(
                createdOn, finishDate, state, code, providerCode,
                providerId, locationId, deposit, lines
        ));
        createInventoryItems(purchase);
        return purchase;
    }

    public Purchase getById(final UUID id) {
        return purchaseRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Purchase> findAll() {
        return purchaseRepository.findAll();
    }

    @Transactional
    public Purchase update(
            final UUID id,
            final LocalDateTime createdOn,
            final LocalDateTime finishDate,
            final PurchaseState state,
            final String code,
            final String providerCode,
            final UUID providerId,
            final UUID locationId,
            final boolean deposit,
            final List<PurchaseLine> lines
    ) {
        final Purchase existingPurchase = getById(id);
        validateReferences(providerId, locationId, lines);
        final Purchase updatedPurchase = new Purchase(
                id,
                createdOn == null ? existingPurchase.createdOn() : createdOn,
                finishDate,
                state == null ? existingPurchase.state() : state,
                code,
                providerCode,
                providerId,
                locationId,
                deposit,
                lines,
                existingPurchase.totalCost()
        ).recalculateTotalCost();

        return purchaseRepository.save(updatedPurchase);
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        itemRepository.deleteAllByOriginPurchaseId(id);
        purchaseRepository.deleteById(id);
    }

    private void createInventoryItems(final Purchase purchase) {
        for (final PurchaseLine line : purchase.lines()) {
            final Article article = getArticle(line.articleId());
            for (int i = 0; i < line.quantity(); i++) {
                final Item purchasedItem = itemRepository.save(Item.create(
                        line.articleId(),
                        line.itemStatus(),
                        null,
                        line.hasChildren(),
                        line.buyPrice(),
                        line.taxId(),
                        null,
                        purchase.providerId(),
                        purchase.locationId(),
                        purchase.id()
                ));

                if (line.hasChildren() && purchasedItem.itemStatus() == ItemStatus.OPENED) {
                    createDescendantItems(purchasedItem, article, purchase.id(), purchase.providerId(), purchase.locationId(), line.taxId());
                }
            }
        }
    }

    /**
     * Recursively traverses the article genealogical tree and creates inventory items
     * for all descendants. At each level, the parent item's cost is divided equally
     * among the total child units (sum of all children quantities).
     */
    private void createDescendantItems(
            final Item parentItem,
            final Article parentArticle,
            final UUID purchaseId,
            final UUID providerId,
            final UUID locationId,
            final UUID buyTaxId
    ) {
        final List<ArticleChild> children = parentArticle.children();
        final int totalChildUnits = children.stream().mapToInt(ArticleChild::quantity).sum();
        final BigDecimal costPerUnit = parentItem.cost()
                .divide(BigDecimal.valueOf(totalChildUnits), MONEY_SCALE, RoundingMode.HALF_UP);

        for (final ArticleChild child : children) {
            final Article childArticle = getArticle(child.childArticleId());
            for (int i = 0; i < child.quantity(); i++) {
                final Item childItem = itemRepository.save(Item.create(
                        child.childArticleId(),
                        ItemStatus.AVAILABLE,
                        parentItem.id(),
                        childArticle.canHaveChildren(),
                        costPerUnit,
                        buyTaxId,
                        null,
                        providerId,
                        locationId,
                        purchaseId
                ));

                // Recurse into grandchildren if the child article itself has children
                if (childArticle.canHaveChildren()) {
                    createDescendantItems(childItem, childArticle, purchaseId, providerId, locationId, buyTaxId);
                }
            }
        }
    }

    private void validateReferences(
            final UUID providerId,
            final UUID locationId,
            final List<PurchaseLine> lines
    ) {
        if (providerRepository.findById(providerId).isEmpty()) {
            throw new NotFoundException(PROVIDER_RESOURCE_NAME, providerId);
        }
        if (locationRepository.findById(locationId).isEmpty()) {
            throw new NotFoundException(LOCATION_RESOURCE_NAME, locationId);
        }
        for (final PurchaseLine line : lines) {
            final Article article = getArticle(line.articleId());
            if (taxRepository.findById(line.taxId()).isEmpty()) {
                throw new NotFoundException(TAX_RESOURCE_NAME, line.taxId());
            }
            validateChildrenConfiguration(article, line.hasChildren());
        }
    }

    private Article getArticle(final UUID articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ARTICLE_RESOURCE_NAME, articleId));
    }

    private void validateChildrenConfiguration(final Article article, final boolean hasChildren) {
        if (!hasChildren) {
            return;
        }
        if (!article.canHaveChildren()) {
            throw new IllegalArgumentException(
                    "purchase line requests child-aware stock but article " + article.id() + " cannot have children"
            );
        }
    }
}
