package com.fractalmindstudio.minerva_core.inventory.item.interfaces.rest;

import com.fractalmindstudio.minerva_core.inventory.item.application.ItemService;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping
    public List<ItemResponse> findAll() {
        return itemService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{itemId}")
    public ItemResponse getById(@PathVariable final UUID itemId) {
        return toResponse(itemService.getById(itemId));
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
