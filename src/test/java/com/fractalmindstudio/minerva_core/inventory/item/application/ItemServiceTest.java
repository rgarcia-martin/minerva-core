package com.fractalmindstudio.minerva_core.inventory.item.application;

import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    private static final UUID ARTICLE_ID = UUID.randomUUID();

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

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
    void shouldReturnRepositoryOrderFromFindAll() {
        final var item1 = Item.create(ARTICLE_ID, ItemStatus.AVAILABLE, null, false, BigDecimal.TEN, null, null, null, null);
        final var item2 = Item.create(ARTICLE_ID, ItemStatus.SOLD, null, false, BigDecimal.ONE, null, null, null, null);
        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));

        final var result = itemService.findAll();

        assertThat(result).containsExactly(item1, item2);
    }
}
