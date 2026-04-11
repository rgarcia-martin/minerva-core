package com.fractalmindstudio.minerva_core.purchasing.purchase.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence.SpringDataArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence.SpringDataItemRepository;
import com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence.SpringDataLocationRepository;
import com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence.SpringDataProviderRepository;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PurchaseRepositoryAdapter implements PurchaseRepository {

    private final SpringDataPurchaseRepository springDataPurchaseRepository;
    private final SpringDataProviderRepository springDataProviderRepository;
    private final SpringDataLocationRepository springDataLocationRepository;
    private final SpringDataArticleRepository springDataArticleRepository;
    private final SpringDataItemRepository springDataItemRepository;
    private final SpringDataTaxRepository springDataTaxRepository;

    public PurchaseRepositoryAdapter(
            final SpringDataPurchaseRepository springDataPurchaseRepository,
            final SpringDataProviderRepository springDataProviderRepository,
            final SpringDataLocationRepository springDataLocationRepository,
            final SpringDataArticleRepository springDataArticleRepository,
            final SpringDataItemRepository springDataItemRepository,
            final SpringDataTaxRepository springDataTaxRepository
    ) {
        this.springDataPurchaseRepository = springDataPurchaseRepository;
        this.springDataProviderRepository = springDataProviderRepository;
        this.springDataLocationRepository = springDataLocationRepository;
        this.springDataArticleRepository = springDataArticleRepository;
        this.springDataItemRepository = springDataItemRepository;
        this.springDataTaxRepository = springDataTaxRepository;
    }

    @Override
    public Purchase save(final Purchase purchase) {
        return toDomain(springDataPurchaseRepository.save(toEntity(purchase)));
    }

    @Override
    public Optional<Purchase> findById(final UUID id) {
        return springDataPurchaseRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<Purchase> findAll() {
        return springDataPurchaseRepository.findAllByOrderByCreatedOnDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataPurchaseRepository.deleteById(UuidMapper.toString(id));
    }

    private PurchaseEntity toEntity(final Purchase purchase) {
        final PurchaseEntity entity = new PurchaseEntity();
        entity.setId(UuidMapper.toString(purchase.id()));
        entity.setCreatedOn(purchase.createdOn());
        entity.setFinishDate(purchase.finishDate());
        entity.setState(purchase.state());
        entity.setCode(purchase.code());
        entity.setProviderCode(purchase.providerCode());
        entity.setProvider(resolveReference(springDataProviderRepository, purchase.providerId(), "provider"));
        entity.setLocation(resolveReference(springDataLocationRepository, purchase.locationId(), "location"));
        entity.setDeposit(purchase.deposit());
        entity.setTotalCost(purchase.totalCost());
        entity.setLines(new ArrayList<>(purchase.lines().stream().map(this::toEntity).toList()));
        return entity;
    }

    private PurchaseLineEntity toEntity(final PurchaseLine purchaseLine) {
        final PurchaseLineEntity entity = new PurchaseLineEntity();
        entity.setId(UuidMapper.toString(purchaseLine.id()));
        entity.setArticle(resolveReference(springDataArticleRepository, purchaseLine.articleId(), "article"));
        entity.setItem(resolveReference(springDataItemRepository, purchaseLine.itemId(), "item"));
        entity.setQuantity(purchaseLine.quantity());
        entity.setBuyPrice(purchaseLine.buyPrice());
        entity.setProfitMargin(purchaseLine.profitMargin());
        entity.setTax(resolveReference(springDataTaxRepository, purchaseLine.taxId(), "tax"));
        entity.setItemStatus(purchaseLine.itemStatus());
        entity.setHasChildren(purchaseLine.hasChildren());
        return entity;
    }

    private Purchase toDomain(final PurchaseEntity entity) {
        return new Purchase(
                UuidMapper.fromString(entity.getId()),
                entity.getCreatedOn(),
                entity.getFinishDate(),
                entity.getState(),
                entity.getCode(),
                entity.getProviderCode(),
                entity.getProvider() != null ? UuidMapper.fromString(entity.getProvider().getId()) : null,
                entity.getLocation() != null ? UuidMapper.fromString(entity.getLocation().getId()) : null,
                entity.isDeposit(),
                entity.getLines().stream().map(this::toDomain).toList(),
                entity.getTotalCost()
        );
    }

    private PurchaseLine toDomain(final PurchaseLineEntity entity) {
        return new PurchaseLine(
                UuidMapper.fromString(entity.getId()),
                entity.getArticle() != null ? UuidMapper.fromString(entity.getArticle().getId()) : null,
                entity.getItem() != null ? UuidMapper.fromString(entity.getItem().getId()) : null,
                entity.getQuantity(),
                entity.getBuyPrice(),
                entity.getProfitMargin(),
                entity.getTax() != null ? UuidMapper.fromString(entity.getTax().getId()) : null,
                entity.getItemStatus(),
                Boolean.TRUE.equals(entity.getHasChildren())
        );
    }

    private static <T> T resolveReference(final JpaRepository<T, String> repository, final UUID id, final String resourceName) {
        if (id == null) {
            return null;
        }
        return repository.findById(UuidMapper.toString(id))
                .orElseThrow(() -> new NotFoundException(resourceName, id));
    }
}
