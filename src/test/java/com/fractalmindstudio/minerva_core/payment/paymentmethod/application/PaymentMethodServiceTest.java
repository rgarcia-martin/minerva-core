package com.fractalmindstudio.minerva_core.payment.paymentmethod.application;

import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethod;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodRepository;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodType;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    @Test
    void shouldCreatePaymentMethod() {
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = paymentMethodService.create("Cash", PaymentMethodType.CASH, "  ");

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Cash");
        assertThat(result.configuration()).isNull();

        final var captor = ArgumentCaptor.forClass(PaymentMethod.class);
        verify(paymentMethodRepository).save(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(PaymentMethodType.CASH);
    }

    @Test
    void shouldGetById() {
        final var paymentMethod = PaymentMethod.create("Card", PaymentMethodType.CARD, "iban");
        when(paymentMethodRepository.findById(paymentMethod.id())).thenReturn(Optional.of(paymentMethod));

        assertThat(paymentMethodService.getById(paymentMethod.id())).isEqualTo(paymentMethod);
    }

    @Test
    void shouldThrowNotFoundWhenMissing() {
        final UUID id = UUID.randomUUID();
        when(paymentMethodRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnRepositoryOrderFromFindAll() {
        final var cash = PaymentMethod.create("Cash", PaymentMethodType.CASH, null);
        final var card = PaymentMethod.create("Card", PaymentMethodType.CARD, "iban");
        when(paymentMethodRepository.findAll()).thenReturn(List.of(card, cash));

        assertThat(paymentMethodService.findAll()).containsExactly(card, cash);
    }

    @Test
    void shouldUpdate() {
        final UUID id = UUID.randomUUID();
        when(paymentMethodRepository.findById(id)).thenReturn(Optional.of(new PaymentMethod(id, "Old", PaymentMethodType.CASH, null)));
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = paymentMethodService.update(id, "Bank Card", PaymentMethodType.CARD, "iban");

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Bank Card");
        assertThat(result.type()).isEqualTo(PaymentMethodType.CARD);
    }

    @Test
    void shouldDelete() {
        final UUID id = UUID.randomUUID();
        when(paymentMethodRepository.findById(id)).thenReturn(Optional.of(new PaymentMethod(id, "Cash", PaymentMethodType.CASH, null)));

        paymentMethodService.delete(id);

        verify(paymentMethodRepository).deleteById(id);
    }
}
