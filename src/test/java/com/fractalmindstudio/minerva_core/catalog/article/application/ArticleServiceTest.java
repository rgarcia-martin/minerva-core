package com.fractalmindstudio.minerva_core.catalog.article.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleChild;
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
                TAX_ID, new BigDecimal("10"), new BigDecimal("15"), List.of()
        );

        assertThat(result.name()).isEqualTo("Widget");
        assertThat(result.code()).isEqualTo("WDG-001");
        assertThat(result.id()).isNotNull();
        assertThat(result.barcode()).isNull();
        assertThat(result.canHaveChildren()).isFalse();

        final var captor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(captor.capture());
        assertThat(captor.getValue().taxId()).isEqualTo(TAX_ID);
    }

    @Test
    void shouldCreateArticleWithMultipleChildren() {
        final UUID childA = UUID.randomUUID();
        final UUID childB = UUID.randomUUID();
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.findById(childA)).thenReturn(Optional.of(
                simpleArticle(childA, "Unit A", "UA-1")
        ));
        when(articleRepository.findById(childB)).thenReturn(Optional.of(
                simpleArticle(childB, "Unit B", "UB-1")
        ));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        final var children = List.of(new ArticleChild(childA, 5), new ArticleChild(childB, 3));
        final var result = articleService.create(
                "Combo", "COMBO-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, children
        );

        assertThat(result.canHaveChildren()).isTrue();
        assertThat(result.children()).hasSize(2);
    }

    @Test
    void shouldValidateAllChildArticleReferencesExist() {
        final UUID childA = UUID.randomUUID();
        final UUID childB = UUID.randomUUID();
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.findById(childA)).thenReturn(Optional.of(
                simpleArticle(childA, "Unit A", "UA-1")
        ));
        when(articleRepository.findById(childB)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(
                "Pack", "P-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(childA, 2), new ArticleChild(childB, 3))
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(childB.toString());
    }

    @Test
    void shouldDetectDirectCycleOnUpdate() {
        // A has child B, updating B to have child A → cycle
        final UUID articleA = UUID.randomUUID();
        final UUID articleB = UUID.randomUUID();

        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.findById(articleB)).thenReturn(Optional.of(
                new Article(articleB, "B", "B-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of())
        ));
        // When cycle detection traverses B's children (currently none), then checks A's proposed children...
        // Actually: we're updating A to have child B. B already has child A in the repo.
        when(articleRepository.findById(articleA)).thenReturn(Optional.of(
                new Article(articleA, "A", "A-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of())
        ));

        // Set up B to have child A (simulating existing state)
        when(articleRepository.findById(articleB)).thenReturn(Optional.of(
                new Article(articleB, "B", "B-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new ArticleChild(articleA, 1)))
        ));

        // Update A to add child B → B already has child A → cycle
        assertThatThrownBy(() -> articleService.update(
                articleA, "A", "A-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(articleB, 1))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle");
    }

    @Test
    void shouldDetectIndirectCycleOnUpdate() {
        // A → B → C, updating C to reference A → cycle
        final UUID articleA = UUID.randomUUID();
        final UUID articleB = UUID.randomUUID();
        final UUID articleC = UUID.randomUUID();

        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));

        // C exists in repo (we are updating it)
        when(articleRepository.findById(articleC)).thenReturn(Optional.of(
                new Article(articleC, "C", "C-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of())
        ));
        // A references B in the repo
        when(articleRepository.findById(articleA)).thenReturn(Optional.of(
                new Article(articleA, "A", "A-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new ArticleChild(articleB, 1)))
        ));
        // B references C in the repo
        when(articleRepository.findById(articleB)).thenReturn(Optional.of(
                new Article(articleB, "B", "B-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new ArticleChild(articleC, 1)))
        ));

        // Update C to add child A → A→B→C→A → cycle
        assertThatThrownBy(() -> articleService.update(
                articleC, "C", "C-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(articleA, 1))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle");
    }

    @Test
    void shouldAllowDiamondTopology() {
        // A → B, A → C, B → D, C → D (no cycle — D is reachable via two paths but no loop)
        final UUID articleB = UUID.randomUUID();
        final UUID articleC = UUID.randomUUID();
        final UUID articleD = UUID.randomUUID();

        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.findById(articleB)).thenReturn(Optional.of(
                new Article(articleB, "B", "B-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new ArticleChild(articleD, 1)))
        ));
        when(articleRepository.findById(articleC)).thenReturn(Optional.of(
                new Article(articleC, "C", "C-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new ArticleChild(articleD, 1)))
        ));
        when(articleRepository.findById(articleD)).thenReturn(Optional.of(
                new Article(articleD, "D", "D-1", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of())
        ));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        // Create A with children B and C — diamond shape, no cycle
        final var result = articleService.create(
                "A", "A-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(articleB, 1), new ArticleChild(articleC, 1))
        );

        assertThat(result.canHaveChildren()).isTrue();
        assertThat(result.children()).hasSize(2);
    }

    @Test
    void shouldThrowNotFoundWhenReferencedTaxDoesNotExist() {
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(
                "Widget", "WDG-001", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of()
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
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(childArticleId, 2))
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(childArticleId.toString());
    }

    @Test
    void shouldGetArticleById() {
        final var article = Article.create("Widget", "WDG", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of());
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
        final var a1 = Article.create("Alpha", "A-code", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of());
        final var a2 = Article.create("Alpha", "B-code", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of());
        when(articleRepository.findAll()).thenReturn(List.of(a1, a2));

        final var result = articleService.findAll();

        assertThat(result).extracting(Article::code).containsExactly("A-code", "B-code");
    }

    @Test
    void shouldUpdateArticle() {
        final var id = UUID.randomUUID();
        final var existing = new Article(id, "Old", "OLD", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of());
        when(articleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = articleService.update(id, "New", "NEW", null, null, "new desc", TAX_ID, new BigDecimal("20"), new BigDecimal("30"), List.of());

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("New");
        assertThat(result.code()).isEqualTo("NEW");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentArticle() {
        final var id = UUID.randomUUID();
        when(articleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(id, "N", "C", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteArticle() {
        final var id = UUID.randomUUID();
        final var article = new Article(id, "Widget", "WDG", null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of());
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

    private static Article simpleArticle(final UUID id, final String name, final String code) {
        return new Article(id, name, code, null, null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of());
    }
}
