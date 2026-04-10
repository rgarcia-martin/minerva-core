package com.fractalmindstudio.minerva_core.purchasing.purchase.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.article.infrastructure.persistence.SpringDataArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence.SpringDataItemRepository;
import com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence.SpringDataLocationRepository;
import com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence.SpringDataProviderRepository;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
        return springDataPurchaseRepository.findAll().stream().map(this::toDomain).toList();
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
        entity.setProvider(resolveReference(springDataProviderRepository, UuidMapper.toString(purchase.providerId())));
        entity.setLocation(resolveReference(springDataLocationRepository, UuidMapper.toString(purchase.locationId())));
        entity.setDeposit(purchase.deposit());
        entity.setTotalCost(purchase.totalCost());
        entity.setLines(purchase.lines().stream().map(this::toEntity).toList());
        return entity;
    }

    private PurchaseLineEntity toEntity(final PurchaseLine purchaseLine) {
        final PurchaseLineEntity entity = new PurchaseLineEntity();
        entity.setId(UuidMapper.toString(purchaseLine.id()));
        entity.setArticle(resolveReference(springDataArticleRepository, UuidMapper.toString(purchaseLine.articleId())));
        entity.setItem(resolveReference(springDataItemRepository, UuidMapper.toString(purchaseLine.itemId())));
        entity.setQuantity(purchaseLine.quantity());
        entity.setBuyPrice(purchaseLine.buyPrice());
        entity.setProfitMargin(purchaseLine.profitMargin());
        entity.setTax(resolveReference(springDataTaxRepository, UuidMapper.toString(purchaseLine.taxId())));
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
                entity.getTax() != null ? UuidMapper.fromString(entity.getTax().getId()) : null
        );
    }

    private static <T> T resolveReference(final JpaRepository<T, String> repository, final String id) {
        return id == null ? null : repository.getReferenceById(id);
    }
}
