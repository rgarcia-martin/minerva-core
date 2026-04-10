package com.fractalmindstudio.minerva_core.catalog.article.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
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

    @InjectMocks
    private ArticleService articleService;

    @Test
    void shouldCreateArticle() {
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = articleService.create(
                "Widget", "WDG-001", "123", null, "desc",
                TAX_ID, new BigDecimal("10"), new BigDecimal("15"), false, 0, null
        );

        assertThat(result.name()).isEqualTo("Widget");
        assertThat(result.code()).isEqualTo("WDG-001");
        assertThat(result.id()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(captor.capture());
        assertThat(captor.getValue().taxId()).isEqualTo(TAX_ID);
    }

    @Test
    void shouldGetArticleById() {
        final var article = Article.create("Widget", "WDG", "123", null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
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
    void shouldFindAllArticlesSortedByNameThenCode() {
        final var a2 = Article.create("Alpha", "B-code", "111", null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        final var a1 = Article.create("Alpha", "A-code", "222", null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        final var b = Article.create("Beta", "C-code", "333", null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        when(articleRepository.findAll()).thenReturn(List.of(a2, b, a1));

        final var result = articleService.findAll();

        assertThat(result).extracting(Article::code).containsExactly("A-code", "B-code", "C-code");
    }

    @Test
    void shouldUpdateArticle() {
        final var id = UUID.randomUUID();
        final var existing = new Article(id, "Old", "OLD", "000", null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
        when(articleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(articleRepository.save(any(Article.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = articleService.update(id, "New", "NEW", "999", null, "new desc", TAX_ID, new BigDecimal("20"), new BigDecimal("30"), false, 0, null);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("New");
        assertThat(result.code()).isEqualTo("NEW");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentArticle() {
        final var id = UUID.randomUUID();
        when(articleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(id, "N", "C", "B", null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteArticle() {
        final var id = UUID.randomUUID();
        final var article = new Article(id, "Widget", "WDG", "123", null, null, TAX_ID, BigDecimal.ONE, BigDecimal.TEN, false, 0, null);
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
