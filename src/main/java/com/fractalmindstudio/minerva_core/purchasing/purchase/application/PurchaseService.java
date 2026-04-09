package com.fractalmindstudio.minerva_core.purchasing.purchase.application;

import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseRepository;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseState;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PurchaseService {

    public static final String RESOURCE_NAME = "purchase";

    private final PurchaseRepository purchaseRepository;

    public PurchaseService(final PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
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
        return purchaseRepository.save(Purchase.create(
                createdOn, finishDate, state, code, providerCode,
                providerId, locationId, deposit, lines
        ));
    }

    public Purchase getById(final UUID id) {
        return purchaseRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Purchase> findAll() {
        return purchaseRepository.findAll().stream()
                .sorted(Comparator.comparing(Purchase::createdOn).reversed())
                .toList();
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
        purchaseRepository.deleteById(id);
    }
}
