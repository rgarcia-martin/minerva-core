package com.fractalmindstudio.minerva_core.catalog.article.interfaces.rest;

import com.fractalmindstudio.minerva_core.catalog.article.application.ArticleService;
import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ArticleController.BASE_PATH)
@Validated
@Log4j2
public class ArticleController {

    public static final String BASE_PATH = "/api/v1/articles";

    private final ArticleService articleService;

    public ArticleController(final ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@Valid @RequestBody final UpsertArticleRequest request) {
        log.debug("create request {}", request);
        final Article article = articleService.create(
                request.name(),
                request.code(),
                request.barcode(),
                request.image(),
                request.description(),
                request.taxId(),
                request.basePrice(),
                request.retailPrice(),
                Boolean.TRUE.equals(request.canHaveChildren()),
                request.numberOfChildren() == null ? 0 : request.numberOfChildren(),
                request.childArticleId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(article));
    }

    @GetMapping
    public List<ArticleResponse> findAll() {
        return articleService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{articleId}")
    public ArticleResponse getById(@PathVariable final UUID articleId) {
        return toResponse(articleService.getById(articleId));
    }

    @PutMapping("/{articleId}")
    public ArticleResponse update(
            @PathVariable final UUID articleId,
            @Valid @RequestBody final UpsertArticleRequest request
    ) {
        final Article article = articleService.update(
                articleId,
                request.name(),
                request.code(),
                request.barcode(),
                request.image(),
                request.description(),
                request.taxId(),
                request.basePrice(),
                request.retailPrice(),
                Boolean.TRUE.equals(request.canHaveChildren()),
                request.numberOfChildren() == null ? 0 : request.numberOfChildren(),
                request.childArticleId()
        );
        return toResponse(article);
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID articleId) {
        articleService.delete(articleId);
        return ResponseEntity.noContent().build();
    }

    private ArticleResponse toResponse(final Article article) {
        return new ArticleResponse(
                article.id(),
                article.name(),
                article.code(),
                article.barcode(),
                article.image(),
                article.description(),
                article.taxId(),
                article.basePrice(),
                article.retailPrice(),
                article.canHaveChildren(),
                article.numberOfChildren(),
                article.childArticleId()
        );
    }

    public record UpsertArticleRequest(
            @NotBlank String name,
            @NotBlank String code,
            String barcode,
            String image,
            String description,
            @NotNull UUID taxId,
            @NotNull @DecimalMin("0.0") BigDecimal basePrice,
            @NotNull @DecimalMin("0.0") BigDecimal retailPrice,
            Boolean canHaveChildren,
            @PositiveOrZero Integer numberOfChildren,
            UUID childArticleId
    ) {
    }

    public record ArticleResponse(
            UUID id,
            String name,
            String code,
            String barcode,
            String image,
            String description,
            UUID taxId,
            BigDecimal basePrice,
            BigDecimal retailPrice,
            boolean canHaveChildren,
            int numberOfChildren,
            UUID childArticleId
    ) {
    }
}
