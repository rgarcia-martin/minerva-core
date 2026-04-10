package com.fractalmindstudio.minerva_core.inventory.item.application;

import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
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
class ItemServiceTest {

    private static final UUID ARTICLE_ID = UUID.randomUUID();

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldCreateItem() {
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = itemService.create(ARTICLE_ID, ItemStatus.AVAILABLE, null, false, new BigDecimal("25"), null, null, null, null);

        assertThat(result.articleId()).isEqualTo(ARTICLE_ID);
        assertThat(result.itemStatus()).isEqualTo(ItemStatus.AVAILABLE);
        assertThat(result.id()).isNotNull();

        final var captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertThat(captor.getValue().articleId()).isEqualTo(ARTICLE_ID);
    }

    @Test
    void shouldGetItemById() {
        final var item = Item.create(ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null);
        when(itemRepository.findById(item.id())).thenReturn(Optional.of(item));

        final var result = itemService.getById(item.id());

        assertThat(result).isEqualTo(item);
    }

    @Test
    void shouldThrowNotFoundWhenItemDoesNotExist() {
        final var id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void shouldFindAllItemsSortedById() {
        final var item1 = Item.create(ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null);
        final var item2 = Item.create(ARTICLE_ID, ItemStatus.SOLD, null, false, BigDecimal.ONE, null, null, null, null);
        when(itemRepository.findAll()).thenReturn(List.of(item2, item1));

        final var result = itemService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isLessThanOrEqualTo(result.get(1).id());
    }

    @Test
    void shouldUpdateItem() {
        final var id = UUID.randomUUID();
        final var existing = new Item(id, ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null);
        when(itemRepository.findById(id)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        final var result = itemService.update(id, ARTICLE_ID, ItemStatus.SOLD, null, false, new BigDecimal("30"), null, null, null, null);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.itemStatus()).isEqualTo(ItemStatus.SOLD);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentItem() {
        final var id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(id, ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteItem() {
        final var id = UUID.randomUUID();
        final var item = new Item(id, ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        itemService.delete(id);

        verify(itemRepository).deleteById(id);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentItem() {
        final var id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
