package com.fractalmindstudio.minerva_core.purchasing.purchase.application;

import com.fractalmindstudio.minerva_core.catalog.article.domain.Article;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleChild;
import com.fractalmindstudio.minerva_core.catalog.article.domain.ArticleRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.Tax;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.inventory.location.domain.Location;
import com.fractalmindstudio.minerva_core.inventory.location.domain.LocationRepository;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.Provider;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.ProviderRepository;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.Purchase;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseLine;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseRepository;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseState;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final UUID LOCATION_ID = UUID.randomUUID();
    private static final UUID ARTICLE_ID = UUID.randomUUID();
    private static final UUID CHILD_ARTICLE_ID = UUID.randomUUID();
    private static final UUID TAX_ID = UUID.randomUUID();

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private TaxRepository taxRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void shouldCreatePurchaseAndGenerateInventoryItems() {
        mockValidProviderAndLocation();
        mockSimpleArticleAndTax();
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = PurchaseLine.create(ARTICLE_ID, 2, new BigDecimal("50"), new BigDecimal("0.10"), TAX_ID);

        final var result = purchaseService.create(
                LocalDateTime.now(), null, PurchaseState.NEW, "PO-001", "PC-001",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );

        assertThat(result.code()).isEqualTo("PO-001");
        assertThat(result.state()).isEqualTo(PurchaseState.NEW);
        assertThat(result.id()).isNotNull();
        assertThat(result.totalCost()).isEqualByComparingTo(new BigDecimal("100.00"));

        verify(purchaseRepository).save(any(Purchase.class));
        final var itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository, times(2)).save(itemCaptor.capture());
        assertThat(itemCaptor.getAllValues())
                .extracting(Item::articleId)
                .containsExactly(ARTICLE_ID, ARTICLE_ID);
        assertThat(itemCaptor.getAllValues())
                .extracting(Item::itemStatus)
                .containsExactly(ItemStatus.AVAILABLE, ItemStatus.AVAILABLE);
        assertThat(itemCaptor.getAllValues())
                .extracting(Item::originPurchaseId)
                .containsOnly(result.id());
    }

    @Test
    void shouldCreatePurchaseWithRecalculatedTotalCost() {
        mockValidProviderAndLocation();
        mockSimpleArticleAndTax();
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = PurchaseLine.create(ARTICLE_ID, 3, new BigDecimal("10"), new BigDecimal("0.20"), TAX_ID);

        final var result = purchaseService.create(
                LocalDateTime.now(), null, null, "PO-002", "PC-002",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );

        assertThat(result.totalCost()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void shouldCreateChildItemsWhenPurchaseRegistersOpenedPackage() {
        mockValidProviderAndLocation();
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(new Article(
                ARTICLE_ID, "Pack", "PACK-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(CHILD_ARTICLE_ID, 2))
        )));
        when(articleRepository.findById(CHILD_ARTICLE_ID)).thenReturn(Optional.of(new Article(
                CHILD_ARTICLE_ID, "Unit", "UNIT-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of()
        )));
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = PurchaseLine.create(
                ARTICLE_ID, 1, new BigDecimal("24.00"), BigDecimal.ZERO, TAX_ID,
                ItemStatus.OPENED, true
        );

        final var result = purchaseService.create(
                LocalDateTime.now(), null, null, "PO-BOX-001", "PC-BOX-001",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );

        assertThat(result.totalCost()).isEqualByComparingTo(new BigDecimal("24.00"));

        final var itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository, times(3)).save(itemCaptor.capture());
        final List<Item> savedItems = itemCaptor.getAllValues();
        final Item parentItem = savedItems.get(0);
        assertThat(parentItem.articleId()).isEqualTo(ARTICLE_ID);
        assertThat(parentItem.itemStatus()).isEqualTo(ItemStatus.OPENED);
        assertThat(parentItem.hasChildren()).isTrue();
        assertThat(savedItems.subList(1, 3))
                .extracting(Item::articleId)
                .containsExactly(CHILD_ARTICLE_ID, CHILD_ARTICLE_ID);
        assertThat(savedItems.subList(1, 3))
                .extracting(Item::cost)
                .containsExactly(new BigDecimal("12.00"), new BigDecimal("12.00"));
        assertThat(savedItems.subList(1, 3))
                .extracting(Item::parentItemId)
                .containsOnly(parentItem.id());
        assertThat(savedItems)
                .extracting(Item::originPurchaseId)
                .containsOnly(result.id());
    }

    @Test
    void shouldCreateGrandchildItemsRecursively() {
        // A has children [B x 2], B has children [C x 3]
        // Purchase 1 unit of A as OPENED → 1 parent (A) + 2 children (B) + 6 grandchildren (C) = 9 items
        final UUID articleA = UUID.randomUUID();
        final UUID articleB = UUID.randomUUID();
        final UUID articleC = UUID.randomUUID();

        mockValidProviderAndLocation();
        when(articleRepository.findById(articleA)).thenReturn(Optional.of(new Article(
                articleA, "Master Box", "MBOX-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(articleB, 2))
        )));
        when(articleRepository.findById(articleB)).thenReturn(Optional.of(new Article(
                articleB, "Sub Box", "SBOX-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(articleC, 3))
        )));
        when(articleRepository.findById(articleC)).thenReturn(Optional.of(new Article(
                articleC, "Unit", "UNIT-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of()
        )));
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        final var line = PurchaseLine.create(
                articleA, 1, new BigDecimal("24.00"), BigDecimal.ZERO, TAX_ID,
                ItemStatus.OPENED, true
        );

        purchaseService.create(
                LocalDateTime.now(), null, null, "PO-GRAND", "PC-GRAND",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );

        // 1 parent (A) + 2 children (B) + 6 grandchildren (C) = 9 items
        final var itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository, times(9)).save(itemCaptor.capture());
        final List<Item> savedItems = itemCaptor.getAllValues();

        // Parent A: cost 24.00 (always first)
        assertThat(savedItems.get(0).articleId()).isEqualTo(articleA);
        assertThat(savedItems.get(0).cost()).isEqualByComparingTo(new BigDecimal("24.00"));
        assertThat(savedItems.get(0).hasChildren()).isTrue();

        // Children B: 2 items, cost = 24.00 / 2 = 12.00 each, hasChildren = true
        final List<Item> bItems = savedItems.stream()
                .filter(i -> i.articleId().equals(articleB)).toList();
        assertThat(bItems).hasSize(2);
        assertThat(bItems).extracting(Item::cost)
                .containsOnly(new BigDecimal("12.00"));
        assertThat(bItems).extracting(Item::hasChildren)
                .containsOnly(true);

        // Grandchildren C: 6 items, cost = 12.00 / 3 = 4.00 each, hasChildren = false
        final List<Item> cItems = savedItems.stream()
                .filter(i -> i.articleId().equals(articleC)).toList();
        assertThat(cItems).hasSize(6);
        assertThat(cItems).extracting(Item::cost)
                .containsOnly(new BigDecimal("4.00"));
        assertThat(cItems).extracting(Item::hasChildren)
                .containsOnly(false);
    }

    @Test
    void shouldCreateItemsForMultipleChildTypes() {
        // A has children [B x 2, C x 3]. Cost = 50.00 → each child costs 50/5 = 10.00
        final UUID articleA = UUID.randomUUID();
        final UUID articleB = UUID.randomUUID();
        final UUID articleC = UUID.randomUUID();

        mockValidProviderAndLocation();
        when(articleRepository.findById(articleA)).thenReturn(Optional.of(new Article(
                articleA, "Combo", "COMBO-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN,
                List.of(new ArticleChild(articleB, 2), new ArticleChild(articleC, 3))
        )));
        when(articleRepository.findById(articleB)).thenReturn(Optional.of(new Article(
                articleB, "Product B", "PB-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of()
        )));
        when(articleRepository.findById(articleC)).thenReturn(Optional.of(new Article(
                articleC, "Product C", "PC-1", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of()
        )));
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        final var line = PurchaseLine.create(
                articleA, 1, new BigDecimal("50.00"), BigDecimal.ZERO, TAX_ID,
                ItemStatus.OPENED, true
        );

        purchaseService.create(
                LocalDateTime.now(), null, null, "PO-MULTI", "PC-MULTI",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );

        // 1 parent + 2 (B) + 3 (C) = 6 items
        final var itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository, times(6)).save(itemCaptor.capture());
        final List<Item> savedItems = itemCaptor.getAllValues();

        // Parent: cost 50.00
        assertThat(savedItems.get(0).cost()).isEqualByComparingTo(new BigDecimal("50.00"));

        // All 5 children cost 10.00 each (50/5)
        final List<Item> childItems = savedItems.subList(1, 6);
        assertThat(childItems).extracting(Item::cost).containsOnly(new BigDecimal("10.00"));

        // 2 items of article B
        assertThat(childItems.stream().filter(i -> i.articleId().equals(articleB)).count()).isEqualTo(2);
        // 3 items of article C
        assertThat(childItems.stream().filter(i -> i.articleId().equals(articleC)).count()).isEqualTo(3);
    }

    @Test
    void shouldRejectChildAwareStockWhenArticleCannotHaveChildren() {
        mockValidProviderAndLocation();
        mockSimpleArticleAndTax();
        final var line = PurchaseLine.create(
                ARTICLE_ID, 1, BigDecimal.TEN, BigDecimal.ZERO, TAX_ID,
                ItemStatus.OPENED, true
        );

        assertThatThrownBy(() -> purchaseService.create(
                LocalDateTime.now(), null, null, "PO-ERR", "PC-ERR",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have children");
    }

    @Test
    void shouldThrowNotFoundWhenProviderDoesNotExist() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.create(
                LocalDateTime.now(), null, null, "PO-001", "PC-001",
                PROVIDER_ID, LOCATION_ID, false, List.of()
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(PROVIDER_ID.toString());
    }

    @Test
    void shouldGetPurchaseById() {
        final var purchase = Purchase.create(LocalDateTime.now(), null, null, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of());
        when(purchaseRepository.findById(purchase.id())).thenReturn(Optional.of(purchase));

        final var result = purchaseService.getById(purchase.id());

        assertThat(result).isEqualTo(purchase);
    }

    @Test
    void shouldThrowNotFoundWhenPurchaseDoesNotExist() {
        final var id = UUID.randomUUID();
        when(purchaseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnRepositoryOrderFromFindAll() {
        final var older = Purchase.create(LocalDateTime.of(2026, 1, 1, 10, 0), null, null, "PO-OLD", "PC-OLD", PROVIDER_ID, LOCATION_ID, false, List.of());
        final var newer = Purchase.create(LocalDateTime.of(2026, 6, 1, 10, 0), null, null, "PO-NEW", "PC-NEW", PROVIDER_ID, LOCATION_ID, false, List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of(newer, older));

        final var result = purchaseService.findAll();

        assertThat(result).extracting(Purchase::code).containsExactly("PO-NEW", "PO-OLD");
    }

    @Test
    void shouldUpdatePurchase() {
        mockValidProviderAndLocation();
        final var id = UUID.randomUUID();
        final var createdOn = LocalDateTime.now();
        final var existing = new Purchase(id, createdOn, null, PurchaseState.NEW, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of(), BigDecimal.ZERO);
        when(purchaseRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = purchaseService.update(id, null, null, PurchaseState.RECEIVED, "PO-UPD", "PC-UPD", PROVIDER_ID, LOCATION_ID, true, List.of());

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.code()).isEqualTo("PO-UPD");
        assertThat(result.state()).isEqualTo(PurchaseState.RECEIVED);
        assertThat(result.createdOn()).isEqualTo(createdOn);
    }

    @Test
    void shouldRecalculateTotalCostOnUpdate() {
        mockValidProviderAndLocation();
        mockSimpleArticleAndTax();
        final var id = UUID.randomUUID();
        final var existing = new Purchase(id, LocalDateTime.now(), null, PurchaseState.NEW, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of(), BigDecimal.ZERO);
        when(purchaseRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = PurchaseLine.create(ARTICLE_ID, 2, new BigDecimal("100"), BigDecimal.ZERO, TAX_ID);

        final var result = purchaseService.update(id, null, null, null, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of(line));

        assertThat(result.totalCost()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentPurchase() {
        final var id = UUID.randomUUID();
        when(purchaseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.update(id, null, null, null, "C", "PC", PROVIDER_ID, LOCATION_ID, false, List.of()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeletePurchaseAndGeneratedInventoryItems() {
        final var id = UUID.randomUUID();
        final var purchase = new Purchase(id, LocalDateTime.now(), null, PurchaseState.NEW, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of(), BigDecimal.ZERO);
        when(purchaseRepository.findById(id)).thenReturn(Optional.of(purchase));

        purchaseService.delete(id);

        verify(itemRepository).deleteAllByOriginPurchaseId(id);
        verify(purchaseRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentPurchase() {
        final var id = UUID.randomUUID();
        when(purchaseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }

    private void mockValidProviderAndLocation() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(Provider.create("Acme", "B111", null, null, null, false)));
        when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(Location.create("Warehouse", null)));
    }

    private void mockSimpleArticleAndTax() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(new Article(
                ARTICLE_ID, "Widget", "WDG", null, null, null,
                TAX_ID, BigDecimal.ONE, BigDecimal.TEN, List.of()
        )));
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
    }
}
