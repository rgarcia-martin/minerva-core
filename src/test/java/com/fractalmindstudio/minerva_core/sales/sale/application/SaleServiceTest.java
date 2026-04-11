package com.fractalmindstudio.minerva_core.sales.sale.application;

import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConcept;
import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConceptRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.Tax;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.identity.user.domain.Role;
import com.fractalmindstudio.minerva_core.identity.user.domain.User;
import com.fractalmindstudio.minerva_core.identity.user.domain.UserRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethod;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodRepository;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodType;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    private static final UUID EMPLOYEE_ID = UUID.randomUUID();
    private static final UUID PAYMENT_METHOD_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();
    private static final UUID FREE_CONCEPT_ID = UUID.randomUUID();
    private static final UUID TAX_ID = UUID.randomUUID();

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private FreeConceptRepository freeConceptRepository;

    @Mock
    private TaxRepository taxRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    void shouldCreateSale() {
        mockValidMainReferences();
        final var line = SaleLine.createForItem(ITEM_ID, new BigDecimal("25"), TAX_ID);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(new Item(
                ITEM_ID, UUID.randomUUID(), ItemStatus.AVAILABLE, null, false, BigDecimal.valueOf(10), null, null, null, null, null
        )));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = saleService.create("SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of(line));

        assertThat(result.code()).isEqualTo("SALE-001");
        assertThat(result.state()).isEqualTo(SaleState.NEW);
        assertThat(result.id()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(captor.capture());
        assertThat(captor.getValue().lines()).hasSize(1);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void shouldCreateSaleWithCalculatedTotalAmount() {
        mockValidMainReferences();
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        final var line = SaleLine.createForFreeConcept(FREE_CONCEPT_ID, 2, new BigDecimal("50"), TAX_ID);
        when(freeConceptRepository.findById(FREE_CONCEPT_ID)).thenReturn(Optional.of(FreeConcept.create("Service", "SVC-1", new BigDecimal("50"), TAX_ID, null)));

        final var result = saleService.create("SALE-002", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of(line));

        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldThrowNotFoundWhenPaymentMethodDoesNotExist() {
        when(userRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(User.create("John", "Doe", "john@test.com", "hash", null, Set.of(Role.READ))));
        when(paymentMethodRepository.findById(PAYMENT_METHOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create("SALE-002", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(PAYMENT_METHOD_ID.toString());
    }

    @Test
    void shouldRejectSellingNonAvailableItem() {
        mockValidMainReferences();
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
        final var line = SaleLine.createForItem(ITEM_ID, new BigDecimal("25"), TAX_ID);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(new Item(
                ITEM_ID, UUID.randomUUID(), ItemStatus.SOLD, null, false, BigDecimal.TEN, null, null, null, null, null
        )));

        assertThatThrownBy(() -> saleService.create("SALE-001", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, List.of(line)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
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
    void shouldReturnRepositoryOrderFromFindAll() {
        final var older = new Sale(UUID.randomUUID(), "SALE-OLD", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, SaleState.NEW, LocalDateTime.of(2026, 1, 1, 10, 0), List.of(), BigDecimal.ZERO);
        final var newer = new Sale(UUID.randomUUID(), "SALE-NEW", EMPLOYEE_ID, null, PAYMENT_METHOD_ID, SaleState.NEW, LocalDateTime.of(2026, 6, 1, 10, 0), List.of(), BigDecimal.ZERO);
        when(saleRepository.findAll()).thenReturn(List.of(newer, older));

        final var result = saleService.findAll();

        assertThat(result).extracting(Sale::code).containsExactly("SALE-NEW", "SALE-OLD");
    }

    @Test
    void shouldDeleteSaleAndReleaseSoldItems() {
        final var id = UUID.randomUUID();
        final var sale = new Sale(
                id,
                "SALE-001",
                EMPLOYEE_ID,
                null,
                PAYMENT_METHOD_ID,
                SaleState.NEW,
                LocalDateTime.now(),
                List.of(SaleLine.createForItem(ITEM_ID, new BigDecimal("25"), TAX_ID)),
                new BigDecimal("25.00")
        );
        when(saleRepository.findById(id)).thenReturn(Optional.of(sale));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(new Item(
                ITEM_ID, UUID.randomUUID(), ItemStatus.SOLD, null, false, BigDecimal.TEN, null, null, null, null, null
        )));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        saleService.delete(id);

        final var itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().itemStatus()).isEqualTo(ItemStatus.AVAILABLE);
        verify(saleRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentSale() {
        final var id = UUID.randomUUID();
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }

    private void mockValidMainReferences() {
        when(userRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(User.create("John", "Doe", "john@test.com", "hash", null, Set.of(Role.READ))));
        when(paymentMethodRepository.findById(PAYMENT_METHOD_ID)).thenReturn(Optional.of(PaymentMethod.create("Cash", PaymentMethodType.CASH, null)));
        when(taxRepository.findById(TAX_ID)).thenReturn(Optional.of(Tax.create("VAT", new BigDecimal("21"), BigDecimal.ZERO)));
    }
}
