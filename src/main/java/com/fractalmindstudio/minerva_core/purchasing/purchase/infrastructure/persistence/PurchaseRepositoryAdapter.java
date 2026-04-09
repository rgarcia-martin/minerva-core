package com.fractalmindstudio.minerva_core.purchasing.purchase.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PurchaseRepositoryAdapter implements PurchaseRepository {

    private final SpringDataPurchaseRepository springDataPurchaseRepository;

    public PurchaseRepositoryAdapter(final SpringDataPurchaseRepository springDataPurchaseRepository) {
        this.springDataPurchaseRepository = springDataPurchaseRepository;
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
        entity.setProviderId(UuidMapper.toString(purchase.providerId()));
        entity.setLocationId(UuidMapper.toString(purchase.locationId()));
        entity.setDeposit(purchase.deposit());
        entity.setTotalCost(purchase.totalCost());
        entity.setLines(purchase.lines().stream().map(this::toEntity).toList());
        return entity;
    }

    private PurchaseLineEntity toEntity(final PurchaseLine purchaseLine) {
        final PurchaseLineEntity entity = new PurchaseLineEntity();
        entity.setId(UuidMapper.toString(purchaseLine.id()));
        entity.setArticleId(UuidMapper.toString(purchaseLine.articleId()));
        entity.setQuantity(purchaseLine.quantity());
        entity.setBuyPrice(purchaseLine.buyPrice());
        entity.setProfitMargin(purchaseLine.profitMargin());
        entity.setTaxId(UuidMapper.toString(purchaseLine.taxId()));
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
                UuidMapper.fromString(entity.getProviderId()),
                UuidMapper.fromString(entity.getLocationId()),
                entity.isDeposit(),
                entity.getLines().stream().map(this::toDomain).toList(),
                entity.getTotalCost()
        );
    }

    private PurchaseLine toDomain(final PurchaseLineEntity entity) {
        return new PurchaseLine(
                UuidMapper.fromString(entity.getId()),
                UuidMapper.fromString(entity.getArticleId()),
                entity.getQuantity(),
                entity.getBuyPrice(),
                entity.getProfitMargin(),
                UuidMapper.fromString(entity.getTaxId())
        );
    }
}
