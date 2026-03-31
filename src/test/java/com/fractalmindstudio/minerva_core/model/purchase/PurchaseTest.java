package com.fractalmindstudio.minerva_core.model.purchase;

import com.fractalmindstudio.minerva_core.model.provider.Provider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseTest {

    private static final UUID PURCHASE_ID = UUID.fromString("89012345-1234-1234-1234-123456789012");
    private static final UUID PROVIDER_ID = UUID.fromString("90123456-1234-1234-1234-123456789012");
    private static final UUID PURCHASE_ITEM_ID = UUID.fromString("01234567-1234-1234-1234-123456789012");
    private static final LocalDateTime CREATED_ON = LocalDateTime.of(2026, 3, 31, 10, 15, 0);
    private static final LocalDateTime FINISH_DATE = LocalDateTime.of(2026, 4, 1, 11, 45, 0);
    private static final String CODE = "PO-2026-001";
    private static final String UPDATED_CODE = "PO-2026-002";
    private static final String PROVIDER_NAME = "Global Provider";
    private static final Float TOTAL_COST = 199.99F;
    private static final Float ITEM_BUY_PRICE = 99.99F;

    @Test
    void shouldCreatePurchaseWithDefaultValuesAndGeneratedId() {
        Purchase purchase = new Purchase();

        assertThat(purchase.getId()).isNotNull();
        assertThat(purchase.getCreatedOn()).isNull();
        assertThat(purchase.getFinishDate()).isNull();
        assertThat(purchase.getState()).isEqualTo(PurchaseState.NEW);
        assertThat(purchase.getCode()).isNull();
        assertThat(purchase.getProvider()).isNull();
        assertThat(purchase.getPurchaseItemList()).isNotNull().isEmpty();
        assertThat(purchase.getTotalCost()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        Provider provider = createProvider();
        PurchaseItem purchaseItem = createPurchaseItem();
        Purchase purchase = new Purchase();

        purchase.setCreatedOn(CREATED_ON);
        purchase.setFinishDate(FINISH_DATE);
        purchase.setState(PurchaseState.PAID);
        purchase.setCode(CODE);
        purchase.setProvider(provider);
        purchase.setPurchaseItemList(new ArrayList<>(List.of(purchaseItem)));
        purchase.setTotalCost(TOTAL_COST);

        assertThat(purchase.getCreatedOn()).isEqualTo(CREATED_ON);
        assertThat(purchase.getFinishDate()).isEqualTo(FINISH_DATE);
        assertThat(purchase.getState()).isEqualTo(PurchaseState.PAID);
        assertThat(purchase.getCode()).isEqualTo(CODE);
        assertThat(purchase.getProvider()).isEqualTo(provider);
        assertThat(purchase.getPurchaseItemList()).containsExactly(purchaseItem);
        assertThat(purchase.getTotalCost()).isEqualTo(TOTAL_COST);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        Provider provider = createProvider();
        PurchaseItem purchaseItem = createPurchaseItem();
        Purchase first = createPurchase(provider, purchaseItem, CODE);
        Purchase second = createPurchase(provider, purchaseItem, CODE);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setCode(UPDATED_CODE);

        assertThat(first).isNotEqualTo(second);
    }

    private Purchase createPurchase(Provider provider, PurchaseItem purchaseItem, String code) {
        Purchase purchase = new Purchase();
        purchase.setId(PURCHASE_ID);
        purchase.setCreatedOn(CREATED_ON);
        purchase.setFinishDate(FINISH_DATE);
        purchase.setState(PurchaseState.PAID);
        purchase.setCode(code);
        purchase.setProvider(provider);
        purchase.setPurchaseItemList(new ArrayList<>(List.of(purchaseItem)));
        purchase.setTotalCost(TOTAL_COST);
        return purchase;
    }

    private Provider createProvider() {
        Provider provider = new Provider();
        provider.setId(PROVIDER_ID);
        provider.setName(PROVIDER_NAME);
        return provider;
    }

    private PurchaseItem createPurchaseItem() {
        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setId(PURCHASE_ITEM_ID);
        purchaseItem.setBuyPrice(ITEM_BUY_PRICE);
        return purchaseItem;
    }
}
