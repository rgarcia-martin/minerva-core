package com.fractalmindstudio.minerva_core.purchasing.provider.application;

import com.fractalmindstudio.minerva_core.purchasing.provider.domain.Provider;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.ProviderRepository;
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
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderService providerService;

    @Test
    void shouldCreateProvider() {
        when(providerRepository.save(any(Provider.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = providerService.create("Acme Corp", "B12345678", "123 St", "555-1234", "acme@test.com", true);

        assertThat(result.businessName()).isEqualTo("Acme Corp");
        assertThat(result.appliesSurcharge()).isTrue();
        assertThat(result.id()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Provider.class);
        verify(providerRepository).save(captor.capture());
        assertThat(captor.getValue().taxIdentifier()).isEqualTo("B12345678");
    }

    @Test
    void shouldGetProviderById() {
        final var provider = Provider.create("Acme", "B111", null, null, null, false);
        when(providerRepository.findById(provider.id())).thenReturn(Optional.of(provider));

        final var result = providerService.getById(provider.id());

        assertThat(result).isEqualTo(provider);
    }

    @Test
    void shouldThrowNotFoundWhenProviderDoesNotExist() {
        final var id = UUID.randomUUID();
        when(providerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnRepositoryOrderFromFindAll() {
        final var alpha = Provider.create("Alpha LLC", "A111", null, null, null, false);
        final var zeta = Provider.create("Zeta Inc", "Z111", null, null, null, false);
        when(providerRepository.findAll()).thenReturn(List.of(alpha, zeta));

        final var result = providerService.findAll();

        assertThat(result).extracting(Provider::businessName).containsExactly("Alpha LLC", "Zeta Inc");
    }

    @Test
    void shouldUpdateProvider() {
        final var id = UUID.randomUUID();
        final var existing = new Provider(id, "Old Corp", "B000", null, null, null, false);
        when(providerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(providerRepository.save(any(Provider.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = providerService.update(id, "New Corp", "B999", "456 Ave", "555-9999", "new@test.com", true);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.businessName()).isEqualTo("New Corp");
        assertThat(result.appliesSurcharge()).isTrue();
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentProvider() {
        final var id = UUID.randomUUID();
        when(providerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.update(id, "N", "T", null, null, null, false))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteProvider() {
        final var id = UUID.randomUUID();
        final var provider = new Provider(id, "Acme", "B111", null, null, null, false);
        when(providerRepository.findById(id)).thenReturn(Optional.of(provider));

        providerService.delete(id);

        verify(providerRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentProvider() {
        final var id = UUID.randomUUID();
        when(providerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
