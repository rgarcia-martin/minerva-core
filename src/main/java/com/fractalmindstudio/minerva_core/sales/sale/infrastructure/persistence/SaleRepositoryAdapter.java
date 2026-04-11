package com.fractalmindstudio.minerva_core.sales.sale.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.catalog.freeconcept.infrastructure.persistence.SpringDataFreeConceptRepository;
import com.fractalmindstudio.minerva_core.catalog.tax.infrastructure.persistence.SpringDataTaxRepository;
import com.fractalmindstudio.minerva_core.identity.user.infrastructure.persistence.SpringDataUserRepository;
import com.fractalmindstudio.minerva_core.inventory.item.infrastructure.persistence.SpringDataItemRepository;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.infrastructure.persistence.SpringDataPaymentMethodRepository;
import com.fractalmindstudio.minerva_core.sales.sale.domain.Sale;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleLine;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SaleRepositoryAdapter implements SaleRepository {

    private final SpringDataSaleRepository springDataSaleRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final SpringDataItemRepository springDataItemRepository;
    private final SpringDataFreeConceptRepository springDataFreeConceptRepository;
    private final SpringDataTaxRepository springDataTaxRepository;
    private final SpringDataPaymentMethodRepository springDataPaymentMethodRepository;

    public SaleRepositoryAdapter(
            final SpringDataSaleRepository springDataSaleRepository,
            final SpringDataUserRepository springDataUserRepository,
            final SpringDataItemRepository springDataItemRepository,
            final SpringDataFreeConceptRepository springDataFreeConceptRepository,
            final SpringDataTaxRepository springDataTaxRepository,
            final SpringDataPaymentMethodRepository springDataPaymentMethodRepository
    ) {
        this.springDataSaleRepository = springDataSaleRepository;
        this.springDataUserRepository = springDataUserRepository;
        this.springDataItemRepository = springDataItemRepository;
        this.springDataFreeConceptRepository = springDataFreeConceptRepository;
        this.springDataTaxRepository = springDataTaxRepository;
        this.springDataPaymentMethodRepository = springDataPaymentMethodRepository;
    }

    @Override
    public Sale save(final Sale sale) {
        return toDomain(springDataSaleRepository.save(toEntity(sale)));
    }

    @Override
    public Optional<Sale> findById(final UUID id) {
        return springDataSaleRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<Sale> findAll() {
        return springDataSaleRepository.findAllByOrderByCreatedOnDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataSaleRepository.deleteById(UuidMapper.toString(id));
    }

    private SaleEntity toEntity(final Sale sale) {
        final SaleEntity entity = new SaleEntity();
        entity.setId(UuidMapper.toString(sale.id()));
        entity.setCode(sale.code());
        entity.setEmployee(resolveReference(springDataUserRepository, sale.employeeId(), "user"));
        entity.setClientId(UuidMapper.toString(sale.clientId()));
        entity.setPaymentMethod(resolveReference(springDataPaymentMethodRepository, sale.paymentMethodId(), "paymentMethod"));
        entity.setState(sale.state());
        entity.setCreatedOn(sale.createdOn());
        entity.setTotalAmount(sale.totalAmount());
        entity.setLines(sale.lines().stream().map(this::toEntity).toList());
        return entity;
    }

    private SaleLineEntity toEntity(final SaleLine saleLine) {
        final SaleLineEntity entity = new SaleLineEntity();
        entity.setId(UuidMapper.toString(saleLine.id()));
        entity.setItem(resolveReference(springDataItemRepository, saleLine.itemId(), "item"));
        entity.setFreeConcept(resolveReference(springDataFreeConceptRepository, saleLine.freeConceptId(), "freeConcept"));
        entity.setQuantity(saleLine.quantity());
        entity.setUnitPrice(saleLine.unitPrice());
        entity.setTax(resolveReference(springDataTaxRepository, saleLine.taxId(), "tax"));
        return entity;
    }

    private Sale toDomain(final SaleEntity entity) {
        return new Sale(
                UuidMapper.fromString(entity.getId()),
                entity.getCode(),
                entity.getEmployee() != null ? UuidMapper.fromString(entity.getEmployee().getId()) : null,
                UuidMapper.fromString(entity.getClientId()),
                entity.getPaymentMethod() != null ? UuidMapper.fromString(entity.getPaymentMethod().getId()) : null,
                entity.getState(),
                entity.getCreatedOn(),
                entity.getLines().stream().map(this::toDomain).toList(),
                entity.getTotalAmount()
        );
    }

    private SaleLine toDomain(final SaleLineEntity entity) {
        return new SaleLine(
                UuidMapper.fromString(entity.getId()),
                entity.getItem() != null ? UuidMapper.fromString(entity.getItem().getId()) : null,
                entity.getFreeConcept() != null ? UuidMapper.fromString(entity.getFreeConcept().getId()) : null,
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getTax() != null ? UuidMapper.fromString(entity.getTax().getId()) : null
        );
    }

    private static <T> T resolveReference(final JpaRepository<T, String> repository, final UUID id, final String resourceName) {
        if (id == null) {
            return null;
        }
        return repository.findById(UuidMapper.toString(id))
                .orElseThrow(() -> new NotFoundException(resourceName, id));
    }
}
