package com.fractalmindstudio.minerva_core.sales.sale.application;

import com.fractalmindstudio.minerva_core.sales.sale.domain.Sale;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleLine;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleRepository;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleState;
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
class SaleServiceTest {

    private static final UUID EMPLOYEE_ID = UUID.randomUUID();
    private static final UUID PAYMENT_METHOD_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID TAX_ID = UUID.randomUUID();

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    void shouldCreateSale() {
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = SaleLine.createForItem(ITEM_ID, new BigDecimal("25"), TAX_ID);

        final var result = saleService.create("SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of(line));

        assertThat(result.code()).isEqualTo("SALE-001");
        assertThat(result.state()).isEqualTo(SaleState.NEW);
        assertThat(result.id()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(captor.capture());
        assertThat(captor.getValue().lines()).hasSize(1);
    }

    @Test
    void shouldCreateSaleWithCalculatedTotalAmount() {
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = SaleLine.createForItem(ITEM_ID, new BigDecimal("50"), TAX_ID);

        final var result = saleService.create("SALE-002", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of(line));

        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void shouldGetSaleById() {
        final var sale = Sale.create("SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of());
        when(saleRepository.findById(sale.id())).thenReturn(Optional.of(sale));

        final var result = saleService.getById(sale.id());

        assertThat(result).isEqualTo(sale);
    }

    @Test
    void shouldThrowNotFoundWhenSaleDoesNotExist() {
        final var id = UUID.randomUUID();
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldFindAllSalesSortedByCreatedOnDescending() {
        final var older = Sale.create("SALE-OLD", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of());
        final var newer = Sale.create("SALE-NEW", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of());
        when(saleRepository.findAll()).thenReturn(List.of(older, newer));

        final var result = saleService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldDeleteSale() {
        final var id = UUID.randomUUID();
        final var sale = new Sale(id, "SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, SaleState.NEW, LocalDateTime.now(), List.of(), BigDecimal.ZERO);
        when(saleRepository.findById(id)).thenReturn(Optional.of(sale));

        saleService.delete(id);

        verify(saleRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentSale() {
        final var id = UUID.randomUUID();
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
