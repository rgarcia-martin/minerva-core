package com.fractalmindstudio.minerva_core.purchasing.purchase.application;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final UUID LOCATION_ID = UUID.randomUUID();
    private static final UUID ARTICLE_ID = UUID.randomUUID();
    private static final UUID TAX_ID = UUID.randomUUID();

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void shouldCreatePurchase() {
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = PurchaseLine.create(ARTICLE_ID, 2, new BigDecimal("50"), new BigDecimal("0.10"), TAX_ID);

        final var result = purchaseService.create(
                LocalDateTime.now(), null, PurchaseState.NEW, "PO-001", "PC-001",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );

        assertThat(result.code()).isEqualTo("PO-001");
        assertThat(result.state()).isEqualTo(PurchaseState.NEW);
        assertThat(result.id()).isNotNull();
        assertThat(result.totalCost()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        assertThat(captor.getValue().lines()).hasSize(1);
    }

    @Test
    void shouldCreatePurchaseWithRecalculatedTotalCost() {
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = PurchaseLine.create(ARTICLE_ID, 3, new BigDecimal("10"), new BigDecimal("0.20"), TAX_ID);

        final var result = purchaseService.create(
                LocalDateTime.now(), null, null, "PO-002", "PC-002",
                PROVIDER_ID, LOCATION_ID, false, List.of(line)
        );

        assertThat(result.totalCost()).isEqualByComparingTo(new BigDecimal("30"));
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
    void shouldFindAllPurchasesSortedByCreatedOnDescending() {
        final var older = Purchase.create(LocalDateTime.of(2026, 1, 1, 10, 0), null, null, "PO-OLD", "PC-OLD", PROVIDER_ID, LOCATION_ID, false, List.of());
        final var newer = Purchase.create(LocalDateTime.of(2026, 6, 1, 10, 0), null, null, "PO-NEW", "PC-NEW", PROVIDER_ID, LOCATION_ID, false, List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of(older, newer));

        final var result = purchaseService.findAll();

        assertThat(result).extracting(Purchase::code).containsExactly("PO-NEW", "PO-OLD");
    }

    @Test
    void shouldUpdatePurchase() {
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
        final var id = UUID.randomUUID();
        final var existing = new Purchase(id, LocalDateTime.now(), null, PurchaseState.NEW, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of(), BigDecimal.ZERO);
        when(purchaseRepository.findById(id)).thenReturn(Optional.of(existing));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = PurchaseLine.create(ARTICLE_ID, 2, new BigDecimal("100"), BigDecimal.ZERO, TAX_ID);

        final var result = purchaseService.update(id, null, null, null, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of(line));

        assertThat(result.totalCost()).isEqualByComparingTo(new BigDecimal("200"));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentPurchase() {
        final var id = UUID.randomUUID();
        when(purchaseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.update(id, null, null, null, "C", "PC", PROVIDER_ID, LOCATION_ID, false, List.of()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeletePurchase() {
        final var id = UUID.randomUUID();
        final var purchase = new Purchase(id, LocalDateTime.now(), null, PurchaseState.NEW, "PO-001", "PC-001", PROVIDER_ID, LOCATION_ID, false, List.of(), BigDecimal.ZERO);
        when(purchaseRepository.findById(id)).thenReturn(Optional.of(purchase));

        purchaseService.delete(id);

        verify(purchaseRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentPurchase() {
        final var id = UUID.randomUUID();
        when(purchaseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
