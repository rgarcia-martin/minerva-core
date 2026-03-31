package com.fractalmindstudio.minerva_core.model.item;

import com.fractalmindstudio.minerva_core.model.article.Article;
import com.fractalmindstudio.minerva_core.model.location.Location;
import com.fractalmindstudio.minerva_core.model.tax.Tax;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    private static final UUID ITEM_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ARTICLE_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID PARENT_ITEM_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID CHILD_ITEM_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID BUY_TAX_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID SPECIAL_TAX_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private static final UUID LOCATION_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    private static final String ARTICLE_NAME = "Monitor";
    private static final String PARENT_ARTICLE_NAME = "Desktop Kit";
    private static final String CHILD_ARTICLE_NAME = "HDMI Cable";
    private static final String LOCATION_NAME = "Shelf A";
    private static final String BUY_TAX_DESCRIPTION = "Buy VAT";
    private static final String SPECIAL_TAX_DESCRIPTION = "Special VAT";
    private static final Float COST = 89.99F;
    private static final Float BUY_TAX_VALUE = 21.0F;
    private static final Float SPECIAL_TAX_VALUE = 4.0F;

    @Test
    void shouldCreateItemWithDefaultValuesAndGeneratedId() {
        Item item = new Item();

        assertThat(item.getId()).isNotNull();
        assertThat(item.getArticle()).isNull();
        assertThat(item.getItemStatus()).isNull();
        assertThat(item.getParent()).isNull();
        assertThat(item.getHasChildren()).isFalse();
        assertThat(item.getChildren()).isNotNull().isEmpty();
        assertThat(item.getCost()).isNull();
        assertThat(item.getBuyTax()).isNull();
        assertThat(item.getSpecialBuyTax()).isNull();
        assertThat(item.getLocation()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        Article article = createArticle(ARTICLE_ID, ARTICLE_NAME);
        Item parent = createRelatedItem(PARENT_ITEM_ID, PARENT_ARTICLE_NAME);
        Item child = createRelatedItem(CHILD_ITEM_ID, CHILD_ARTICLE_NAME);
        Tax buyTax = createTax(BUY_TAX_ID, BUY_TAX_DESCRIPTION, BUY_TAX_VALUE);
        Tax specialBuyTax = createTax(SPECIAL_TAX_ID, SPECIAL_TAX_DESCRIPTION, SPECIAL_TAX_VALUE);
        Location location = createLocation();
        Item item = new Item();

        item.setArticle(article);
        item.setItemStatus(ItemStatus.AVAILABLE);
        item.setParent(parent);
        item.setHasChildren(Boolean.TRUE);
        item.setChildren(new ArrayList<>(List.of(child)));
        item.setCost(COST);
        item.setBuyTax(buyTax);
        item.setSpecialBuyTax(specialBuyTax);
        item.setLocation(location);

        assertThat(item.getArticle()).isEqualTo(article);
        assertThat(item.getItemStatus()).isEqualTo(ItemStatus.AVAILABLE);
        assertThat(item.getParent()).isEqualTo(parent);
        assertThat(item.getHasChildren()).isTrue();
        assertThat(item.getChildren()).containsExactly(child);
        assertThat(item.getCost()).isEqualTo(COST);
        assertThat(item.getBuyTax()).isEqualTo(buyTax);
        assertThat(item.getSpecialBuyTax()).isEqualTo(specialBuyTax);
        assertThat(item.getLocation()).isEqualTo(location);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        Article article = createArticle(ARTICLE_ID, ARTICLE_NAME);
        Item parent = createRelatedItem(PARENT_ITEM_ID, PARENT_ARTICLE_NAME);
        Item child = createRelatedItem(CHILD_ITEM_ID, CHILD_ARTICLE_NAME);
        Tax buyTax = createTax(BUY_TAX_ID, BUY_TAX_DESCRIPTION, BUY_TAX_VALUE);
        Tax specialBuyTax = createTax(SPECIAL_TAX_ID, SPECIAL_TAX_DESCRIPTION, SPECIAL_TAX_VALUE);
        Location location = createLocation();
        Item first = createItem(article, parent, child, buyTax, specialBuyTax, location, ItemStatus.AVAILABLE);
        Item second = createItem(article, parent, child, buyTax, specialBuyTax, location, ItemStatus.AVAILABLE);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setItemStatus(ItemStatus.RESERVED);

        assertThat(first).isNotEqualTo(second);
    }

    private Item createItem(Article article, Item parent, Item child, Tax buyTax, Tax specialBuyTax, Location location,
                            ItemStatus status) {
        Item item = new Item();
        item.setId(ITEM_ID);
        item.setArticle(article);
        item.setItemStatus(status);
        item.setParent(parent);
        item.setHasChildren(Boolean.TRUE);
        item.setChildren(new ArrayList<>(List.of(child)));
        item.setCost(COST);
        item.setBuyTax(buyTax);
        item.setSpecialBuyTax(specialBuyTax);
        item.setLocation(location);
        return item;
    }

    private Item createRelatedItem(UUID itemId, String articleName) {
        Item item = new Item();
        item.setId(itemId);
        item.setArticle(createArticle(UUID.randomUUID(), articleName));
        return item;
    }

    private Article createArticle(UUID articleId, String name) {
        Article article = new Article();
        article.setId(articleId);
        article.setName(name);
        return article;
    }

    private Tax createTax(UUID taxId, String description, Float taxValue) {
        Tax tax = new Tax();
        tax.setId(taxId);
        tax.setDescription(description);
        tax.setTax(taxValue);
        return tax;
    }

    private Location createLocation() {
        Location location = new Location();
        location.setId(LOCATION_ID);
        location.setName(LOCATION_NAME);
        return location;
    }
}
