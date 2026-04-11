package com.fractalmindstudio.minerva_core.sales.sale.application;

import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.sales.sale.domain.Sale;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleLine;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SaleService {

    public static final String RESOURCE_NAME = "sale";
    public static final String ITEM_RESOURCE_NAME = "item";

    private final SaleRepository saleRepository;
    private final ItemRepository itemRepository;

    public SaleService(final SaleRepository saleRepository, final ItemRepository itemRepository) {
        this.saleRepository = saleRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Sale create(
            final String code,
            final UUID employeeId,
            final UUID clientId,
            final UUID paymentMethodId,
            final List<SaleLine> lines
    ) {
        validateItemsAvailable(lines);

        final Sale sale = saleRepository.save(Sale.create(code, employeeId, clientId, paymentMethodId, lines));

        markItemsAsSold(lines);

        return sale;
    }

    private void validateItemsAvailable(final List<SaleLine> lines) {
        for (final SaleLine line : lines) {
            if (line.itemId() != null) {
                final Item item = itemRepository.findById(line.itemId())
                        .orElseThrow(() -> new NotFoundException(ITEM_RESOURCE_NAME, line.itemId()));
                if (item.itemStatus() != ItemStatus.AVAILABLE) {
                    throw new IllegalArgumentException(
                            "Item " + line.itemId() + " is not available for sale (status: " + item.itemStatus() + ")"
                    );
                }
            }
        }
    }

    private void markItemsAsSold(final List<SaleLine> lines) {
        for (final SaleLine line : lines) {
            if (line.itemId() != null) {
                final Item item = itemRepository.findById(line.itemId()).orElseThrow();
                itemRepository.save(item.markAsSold());
            }
        }
    }

    public Sale getById(final UUID id) {
        return saleRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Sale> findAll() {
        return saleRepository.findAll().stream()
                .sorted(Comparator.comparing(Sale::createdOn).reversed())
                .toList();
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        saleRepository.deleteById(id);
    }
}
