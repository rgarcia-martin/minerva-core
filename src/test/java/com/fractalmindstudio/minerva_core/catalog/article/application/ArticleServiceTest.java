package com.fractalmindstudio.minerva_core.catalog.article.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.Tax;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    private static final UUID TAX_ID = UUID.randomUUID();

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private TaxRepository taxRepository;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void shouldCreateArticle() {
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = articleService.create(
                "Widget", "WDG-001", null, null, "desc",
                TAX_ID, new BigDecimal("10"), new BigDecimal("15"), false, 0, null
        );

        assertThat(result.name()).isEqualTo("Widget");
        assertThat(result.code()).isEqualTo("WDG-001");
        assertThat(result.id()).isNotNull();
        assertThat(result.barcode()).isNull();

        final var captor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(captor.capture());
        assertThat(captor.getValue().taxId()).isEqualTo(TAX_ID);
    }

    @Test
    void shouldValidateTaxAndChildArticleOnCreate() {
        final UUID childArticleId = UUID.randomUUID();
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.findById(childArticleId)).thenReturn(Optional.of(Article.create(
                "Unit", "U-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null
        )));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = articleService.create(
                "Pack", "P-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, true, 2, childArticleId
        );

        assertThat(result.childArticleId()).isEqualTo(childArticleId);
        assertThat(result.canHaveChildren()).isTrue();
    }

    @Test
    void shouldThrowNotFoundWhenReferencedTaxDoesNotExist() {
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(
                "Widget", "WDG-001", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("tax");
    }

    @Test
    void shouldThrowNotFoundWhenReferencedChildArticleDoesNotExist() {
        final UUID childArticleId = UUID.randomUUID();
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.findById(childArticleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(
                "Pack", "P-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, true, 2, childArticleId
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(childArticleId.toString());
    }

    @Test
    void shouldGetArticleById() {
        final var article = Article.create("Widget", "WDG", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        when(articleRepository.findById(article.id())).thenReturn(Optional.of(article));

        final var result = articleService.getById(article.id());

        assertThat(result).isEqualTo(article);
    }

    @Test
    void shouldThrowNotFoundWhenArticleDoesNotExist() {
        final var id = UUID.randomUUID();
        when(articleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnRepositoryOrderFromFindAll() {
        final var a1 = Article.create("Alpha", "A-code", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        final var a2 = Article.create("Alpha", "B-code", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        when(articleRepository.findAll()).thenReturn(List.of(a1, a2));

        final var result = articleService.findAll();

        assertThat(result).extracting(Article::code).containsExactly("A-code", "B-code");
    }

    @Test
    void shouldUpdateArticle() {
        final var id = UUID.randomUUID();
        final var existing = new Article(id, "Old", "OLD", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        when(articleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = articleService.update(id, "New", "NEW", null, null, "new desc", TAX_ID, new BigDecimal("20"), new BigDecimal("30"), false, 0, null);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("New");
        assertThat(result.code()).isEqualTo("NEW");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentArticle() {
        final var id = UUID.randomUUID();
        when(articleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(id, "N", "C", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteArticle() {
        final var id = UUID.randomUUID();
        final var article = new Article(id, "Widget", "WDG", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        when(articleRepository.findById(id)).thenReturn(Optional.of(article));

        articleService.delete(id);

        verify(articleRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentArticle() {
        final var id = UUID.randomUUID();
        when(articleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
