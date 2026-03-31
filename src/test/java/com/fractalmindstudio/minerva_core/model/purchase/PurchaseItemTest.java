package com.fractalmindstudio.minerva_core.model.purchase;

import com.fractalmindstudio.minerva_core.model.article.Article;
import com.fractalmindstudio.minerva_core.model.item.Item;
import com.fractalmindstudio.minerva_core.model.tax.Tax;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseItemTest {

    private static final UUID PURCHASE_ITEM_ID = UUID.fromString("23456789-1234-1234-1234-123456789012");
    private static final UUID ITEM_ID = UUID.fromString("34567890-1234-1234-1234-123456789012");
    private static final UUID ARTICLE_ID = UUID.fromString("45678901-1234-1234-1234-123456789012");
    private static final UUID PURCHASE_ID = UUID.fromString("56789012-1234-1234-1234-123456789012");
    private static final UUID TAX_ID = UUID.fromString("67890123-1234-1234-1234-123456789012");
    private static final UUID SPECIAL_TAX_ID = UUID.fromString("78901234-1234-1234-1234-123456789012");
    private static final String ARTICLE_NAME = "Keyboard";
    private static final String PURCHASE_CODE = "PO-001";
    private static final String TAX_DESCRIPTION = "Tax";
    private static final String SPECIAL_TAX_DESCRIPTION = "Special Tax";
    private static final Float BUY_PRICE = 35.5F;
    private static final Float TAX_VALUE = 21.0F;
    private static final Float SPECIAL_TAX_VALUE = 8.0F;
    private static final Float UPDATED_BUY_PRICE = 36.5F;

    @Test
    void shouldCreatePurchaseItemWithDefaultValuesAndGeneratedId() {
        PurchaseItem purchaseItem = new PurchaseItem();

        assertThat(purchaseItem.getId()).isNotNull();
        assertThat(purchaseItem.getItem()).isNull();
        assertThat(purchaseItem.getPurchase()).isNull();
        assertThat(purchaseItem.getTax()).isNull();
        assertThat(purchaseItem.getSpecialTax()).isNull();
        assertThat(purchaseItem.getBuyPrice()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        Item item = createItem();
        Purchase purchase = createPurchase();
        Tax tax = createTax(TAX_ID, TAX_DESCRIPTION, TAX_VALUE);
        Tax specialTax = createTax(SPECIAL_TAX_ID, SPECIAL_TAX_DESCRIPTION, SPECIAL_TAX_VALUE);
        PurchaseItem purchaseItem = new PurchaseItem();

        purchaseItem.setItem(item);
        purchaseItem.setPurchase(purchase);
        purchaseItem.setTax(tax);
        purchaseItem.setSpecialTax(specialTax);
        purchaseItem.setBuyPrice(BUY_PRICE);

        assertThat(purchaseItem.getItem()).isEqualTo(item);
        assertThat(purchaseItem.getPurchase()).isEqualTo(purchase);
        assertThat(purchaseItem.getTax()).isEqualTo(tax);
        assertThat(purchaseItem.getSpecialTax()).isEqualTo(specialTax);
        assertThat(purchaseItem.getBuyPrice()).isEqualTo(BUY_PRICE);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        Item item = createItem();
        Purchase purchase = createPurchase();
        Tax tax = createTax(TAX_ID, TAX_DESCRIPTION, TAX_VALUE);
        Tax specialTax = createTax(SPECIAL_TAX_ID, SPECIAL_TAX_DESCRIPTION, SPECIAL_TAX_VALUE);
        PurchaseItem first = createPurchaseItem(item, purchase, tax, specialTax, BUY_PRICE);
        PurchaseItem second = createPurchaseItem(item, purchase, tax, specialTax, BUY_PRICE);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setBuyPrice(UPDATED_BUY_PRICE);

        assertThat(first).isNotEqualTo(second);
    }

    private PurchaseItem createPurchaseItem(Item item, Purchase purchase, Tax tax, Tax specialTax, Float buyPrice) {
        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setId(PURCHASE_ITEM_ID);
        purchaseItem.setItem(item);
        purchaseItem.setPurchase(purchase);
        purchaseItem.setTax(tax);
        purchaseItem.setSpecialTax(specialTax);
        purchaseItem.setBuyPrice(buyPrice);
        return purchaseItem;
    }

    private Item createItem() {
        Article article = new Article();
        article.setId(ARTICLE_ID);
        article.setName(ARTICLE_NAME);

        Item item = new Item();
        item.setId(ITEM_ID);
        item.setArticle(article);
        return item;
    }

    private Purchase createPurchase() {
        Purchase purchase = new Purchase();
        purchase.setId(PURCHASE_ID);
        purchase.setCode(PURCHASE_CODE);
        return purchase;
    }

    private Tax createTax(UUID taxId, String description, Float value) {
        Tax tax = new Tax();
        tax.setId(taxId);
        tax.setDescription(description);
        tax.setTax(value);
        return tax;
    }
}
