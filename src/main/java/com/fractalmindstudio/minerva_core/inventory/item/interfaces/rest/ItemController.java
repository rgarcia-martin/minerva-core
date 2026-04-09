package com.fractalmindstudio.minerva_core.inventory.item.interfaces.rest;

import com.fractalmindstudio.minerva_core.inventory.item.application.ItemService;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping(ItemController.BASE_PATH)
@Validated
public class ItemController {

    public static final String BASE_PATH = "/api/v1/items";

    private final ItemService itemService;

    public ItemController(final ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody final UpsertItemRequest request) {
        final Item item = itemService.create(
                request.articleId(),
                request.itemStatus(),
                request.parentItemId(),
                Boolean.TRUE.equals(request.hasChildren()),
                request.cost(),
                request.buyTaxId(),
                request.specialBuyTaxId(),
                request.providerId(),
                request.locationId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(item));
    }

    @GetMapping
    public List<ItemResponse> findAll() {
        return itemService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{itemId}")
    public ItemResponse getById(@PathVariable final UUID itemId) {
        return toResponse(itemService.getById(itemId));
    }

    @PutMapping("/{itemId}")
    public ItemResponse update(
            @PathVariable final UUID itemId,
            @Valid @RequestBody final UpsertItemRequest request
    ) {
        final Item item = itemService.update(
                itemId,
                request.articleId(),
                request.itemStatus(),
                request.parentItemId(),
                Boolean.TRUE.equals(request.hasChildren()),
                request.cost(),
                request.buyTaxId(),
                request.specialBuyTaxId(),
                request.providerId(),
                request.locationId()
        );
        return toResponse(item);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID itemId) {
        itemService.delete(itemId);
        return ResponseEntity.noContent().build();
    }

    private ItemResponse toResponse(final Item item) {
        return new ItemResponse(
                item.id(),
                item.articleId(),
                item.itemStatus(),
                item.parentItemId(),
                item.hasChildren(),
                item.cost(),
                item.buyTaxId(),
                item.specialBuyTaxId(),
                item.providerId(),
                item.locationId()
        );
    }

    public record UpsertItemRequest(
            @NotNull UUID articleId,
            ItemStatus itemStatus,
            UUID parentItemId,
            Boolean hasChildren,
            @NotNull @DecimalMin("0.0") BigDecimal cost,
            UUID buyTaxId,
            UUID specialBuyTaxId,
            UUID providerId,
            UUID locationId
    ) {
    }

    public record ItemResponse(
            UUID id,
            UUID articleId,
            ItemStatus itemStatus,
            UUID parentItemId,
            boolean hasChildren,
            BigDecimal cost,
            UUID buyTaxId,
            UUID specialBuyTaxId,
            UUID providerId,
            UUID locationId
    ) {
    }
}
