package com.fractalmindstudio.minerva_core.model.article;

import com.fractalmindstudio.minerva_core.model.tax.Tax;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {

    private static final UUID ARTICLE_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID TAX_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID PARENT_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID CHILD_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final String NAME = "Console";
    private static final String UPDATED_NAME = "Handheld Console";
    private static final String CODE = "CON-001";
    private static final String IMAGE = "console.png";
    private static final String DESCRIPTION = "Gaming console";
    private static final String TAX_DESCRIPTION = "VAT";
    private static final String PARENT_NAME = "Bundle";
    private static final String CHILD_NAME = "Controller";
    private static final Float PRICE_BASE = 100.0F;
    private static final Float PRICE_PVP = 121.0F;
    private static final Float TAX_VALUE = 21.0F;
    private static final Integer NUMBER_OF_CHILDREN = 1;

    @Test
    void shouldCreateArticleWithDefaultValuesAndGeneratedId() {
        Article article = new Article();

        assertThat(article.getId()).isNotNull();
        assertThat(article.getName()).isNull();
        assertThat(article.getCode()).isNull();
        assertThat(article.getImage()).isNull();
        assertThat(article.getDescription()).isNull();
        assertThat(article.getTax()).isNull();
        assertThat(article.getPriceBase()).isNull();
        assertThat(article.getPricePVP()).isNull();
        assertThat(article.getCouldHaveChildren()).isFalse();
        assertThat(article.getNumberOfChildren()).isZero();
        assertThat(article.getParent()).isNull();
        assertThat(article.getChild()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        Tax tax = createTax();
        Article parent = createRelatedArticle(PARENT_ID, PARENT_NAME);
        Article child = createRelatedArticle(CHILD_ID, CHILD_NAME);
        Article article = new Article();

        article.setName(NAME);
        article.setCode(CODE);
        article.setImage(IMAGE);
        article.setDescription(DESCRIPTION);
        article.setTax(tax);
        article.setPriceBase(PRICE_BASE);
        article.setPricePVP(PRICE_PVP);
        article.setCouldHaveChildren(Boolean.TRUE);
        article.setNumberOfChildren(NUMBER_OF_CHILDREN);
        article.setParent(parent);
        article.setChild(child);

        assertThat(article.getName()).isEqualTo(NAME);
        assertThat(article.getCode()).isEqualTo(CODE);
        assertThat(article.getImage()).isEqualTo(IMAGE);
        assertThat(article.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(article.getTax()).isEqualTo(tax);
        assertThat(article.getPriceBase()).isEqualTo(PRICE_BASE);
        assertThat(article.getPricePVP()).isEqualTo(PRICE_PVP);
        assertThat(article.getCouldHaveChildren()).isTrue();
        assertThat(article.getNumberOfChildren()).isEqualTo(NUMBER_OF_CHILDREN);
        assertThat(article.getParent()).isEqualTo(parent);
        assertThat(article.getChild()).isEqualTo(child);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        Tax tax = createTax();
        Article parent = createRelatedArticle(PARENT_ID, PARENT_NAME);
        Article child = createRelatedArticle(CHILD_ID, CHILD_NAME);
        Article first = createArticle(tax, parent, child);
        Article second = createArticle(tax, parent, child);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setName(UPDATED_NAME);

        assertThat(first).isNotEqualTo(second);
    }

    private Article createArticle(Tax tax, Article parent, Article child) {
        Article article = new Article();
        article.setId(ARTICLE_ID);
        article.setName(NAME);
        article.setCode(CODE);
        article.setImage(IMAGE);
        article.setDescription(DESCRIPTION);
        article.setTax(tax);
        article.setPriceBase(PRICE_BASE);
        article.setPricePVP(PRICE_PVP);
        article.setCouldHaveChildren(Boolean.TRUE);
        article.setNumberOfChildren(NUMBER_OF_CHILDREN);
        article.setParent(parent);
        article.setChild(child);
        return article;
    }

    private Article createRelatedArticle(UUID id, String name) {
        Article article = new Article();
        article.setId(id);
        article.setName(name);
        return article;
    }

    private Tax createTax() {
        Tax tax = new Tax();
        tax.setId(TAX_ID);
        tax.setDescription(TAX_DESCRIPTION);
        tax.setTax(TAX_VALUE);
        return tax;
    }
}
