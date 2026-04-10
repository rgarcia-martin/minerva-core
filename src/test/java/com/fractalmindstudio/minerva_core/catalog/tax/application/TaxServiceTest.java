package com.fractalmindstudio.minerva_core.catalog.tax.application;

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
class TaxServiceTest {

    @Mock
    private TaxRepository taxRepository;

    @InjectMocks
    private TaxService taxService;

    @Test
    void shouldCreateTax() {
        when(taxRepository.save(any(Tax.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = taxService.create("IVA", new BigDecimal("21"), new BigDecimal("5.2"));

        assertThat(result.description()).isEqualTo("IVA");
        assertThat(result.id()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Tax.class);
        verify(taxRepository).save(captor.capture());
        assertThat(captor.getValue().description()).isEqualTo("IVA");
    }

    @Test
    void shouldGetTaxById() {
        final var tax = Tax.create("IVA", new BigDecimal("21"), new BigDecimal("5.2"));
        when(taxRepository.findById(tax.id())).thenReturn(Optional.of(tax));

        final var result = taxService.getById(tax.id());

        assertThat(result).isEqualTo(tax);
    }

    @Test
    void shouldThrowNotFoundWhenTaxDoesNotExist() {
        final var id = UUID.randomUUID();
        when(taxRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldFindAllTaxesSortedByDescription() {
        final var reduced = Tax.create("Reduced", new BigDecimal("10"), BigDecimal.ZERO);
        final var general = Tax.create("General", new BigDecimal("21"), BigDecimal.ZERO);
        when(taxRepository.findAll()).thenReturn(List.of(reduced, general));

        final var result = taxService.findAll();

        assertThat(result).extracting(Tax::description).containsExactly("General", "Reduced");
    }

    @Test
    void shouldUpdateTax() {
        final var id = UUID.randomUUID();
        final var existing = new Tax(id, "Old", new BigDecimal("10"), BigDecimal.ZERO);
        when(taxRepository.findById(id)).thenReturn(Optional.of(existing));
        when(taxRepository.save(any(Tax.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = taxService.update(id, "Updated", new BigDecimal("21"), new BigDecimal("5.2"));

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.description()).isEqualTo("Updated");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentTax() {
        final var id = UUID.randomUUID();
        when(taxRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxService.update(id, "X", BigDecimal.ONE, BigDecimal.ZERO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteTax() {
        final var id = UUID.randomUUID();
        final var tax = new Tax(id, "IVA", new BigDecimal("21"), BigDecimal.ZERO);
        when(taxRepository.findById(id)).thenReturn(Optional.of(tax));

        taxService.delete(id);

        verify(taxRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentTax() {
        final var id = UUID.randomUUID();
        when(taxRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
