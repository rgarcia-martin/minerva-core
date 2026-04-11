package com.fractalmindstudio.minerva_core.sales.sale.application;

import com.fractalmindstudio.minerva_core.catalog.freeconcept.domain.FreeConceptRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.domain.TaxRepository;
import com.fractalmindstudio.minerva_core.identity.user.domain.UserRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.Item;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemRepository;
import com.fractalmindstudio.minerva_core.inventory.item.domain.ItemStatus;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodRepository;
import com.fractalmindstudio.minerva_core.sales.sale.domain.Sale;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleLine;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SaleService {

    public static final String RESOURCE_NAME = "sale";
    public static final String ITEM_RESOURCE_NAME = "item";
    public static final String USER_RESOURCE_NAME = "user";
    public static final String PAYMENT_METHOD_RESOURCE_NAME = "paymentMethod";
    public static final String FREE_CONCEPT_RESOURCE_NAME = "freeConcept";
    public static final String TAX_RESOURCE_NAME = "tax";

    private final SaleRepository saleRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final FreeConceptRepository freeConceptRepository;
    private final TaxRepository taxRepository;

    public SaleService(
            final SaleRepository saleRepository,
            final ItemRepository itemRepository,
            final UserRepository userRepository,
            final PaymentMethodRepository paymentMethodRepository,
            final FreeConceptRepository freeConceptRepository,
            final TaxRepository taxRepository
    ) {
        this.saleRepository = saleRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.freeConceptRepository = freeConceptRepository;
        this.taxRepository = taxRepository;
    }

    @Transactional
    public Sale create(
            final String code,
            final UUID employeeId,
            final UUID clientId,
            final UUID paymentMethodId,
            final List<SaleLine> lines
    ) {
        validateMainReferences(employeeId, paymentMethodId);
        final Map<UUID, Item> itemsToSell = validateLines(lines);

        final Sale sale = saleRepository.save(Sale.create(code, employeeId, clientId, paymentMethodId, lines));

        markItemsAsSold(itemsToSell);

        return sale;
    }

    private void validateMainReferences(final UUID employeeId, final UUID paymentMethodId) {
        if (userRepository.findById(employeeId).isEmpty()) {
            throw new NotFoundException(USER_RESOURCE_NAME, employeeId);
        }
        if (paymentMethodRepository.findById(paymentMethodId).isEmpty()) {
            throw new NotFoundException(PAYMENT_METHOD_RESOURCE_NAME, paymentMethodId);
        }
    }

    private Map<UUID, Item> validateLines(final List<SaleLine> lines) {
        final Map<UUID, Item> itemsById = new HashMap<>();

        for (final SaleLine line : lines) {
            if (taxRepository.findById(line.taxId()).isEmpty()) {
                throw new NotFoundException(TAX_RESOURCE_NAME, line.taxId());
            }

            if (line.itemId() != null) {
                final Item item = itemRepository.findById(line.itemId())
                        .orElseThrow(() -> new NotFoundException(ITEM_RESOURCE_NAME, line.itemId()));
                if (item.itemStatus() != ItemStatus.AVAILABLE) {
                    throw new IllegalArgumentException(
                            "Item " + line.itemId() + " is not available for sale (status: " + item.itemStatus() + ")"
                    );
                }
                itemsById.put(line.itemId(), item);
            }

            if (line.freeConceptId() != null && freeConceptRepository.findById(line.freeConceptId()).isEmpty()) {
                throw new NotFoundException(FREE_CONCEPT_RESOURCE_NAME, line.freeConceptId());
            }
        }

        return itemsById;
    }

    private void markItemsAsSold(final Map<UUID, Item> itemsById) {
        for (final Item item : itemsById.values()) {
            itemRepository.save(item.markAsSold());
        }
    }


    private void releaseSoldItems(final Sale sale) {
        for (final SaleLine line : sale.lines()) {
            if (line.itemId() == null) {
                continue;
            }
            final Item item = itemRepository.findById(line.itemId())
                    .orElseThrow(() -> new NotFoundException(ITEM_RESOURCE_NAME, line.itemId()));
            itemRepository.save(item.markAsAvailable());
        }
    }

    public Sale getById(final UUID id) {
        return saleRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    @Transactional
    public void delete(final UUID id) {
        final Sale sale = getById(id);
        releaseSoldItems(sale);
        saleRepository.deleteById(id);
    }
}
