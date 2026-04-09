package com.fractalmindstudio.minerva_core.sales.sale.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.sales.sale.domain.Sale;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleLine;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SaleRepositoryAdapter implements SaleRepository {

    private final SpringDataSaleRepository springDataSaleRepository;

    public SaleRepositoryAdapter(final SpringDataSaleRepository springDataSaleRepository) {
        this.springDataSaleRepository = springDataSaleRepository;
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
        return springDataSaleRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataSaleRepository.deleteById(UuidMapper.toString(id));
    }

    private SaleEntity toEntity(final Sale sale) {
        final SaleEntity entity = new SaleEntity();
        entity.setId(UuidMapper.toString(sale.id()));
        entity.setCode(sale.code());
        entity.setEmployeeId(UuidMapper.toString(sale.employeeId()));
        entity.setClientId(UuidMapper.toString(sale.clientId()));
        entity.setPaymentMethodId(UuidMapper.toString(sale.paymentMethodId()));
        entity.setState(sale.state());
        entity.setCreatedOn(sale.createdOn());
        entity.setTotalAmount(sale.totalAmount());
        entity.setLines(sale.lines().stream().map(this::toEntity).toList());
        return entity;
    }

    private SaleLineEntity toEntity(final SaleLine saleLine) {
        final SaleLineEntity entity = new SaleLineEntity();
        entity.setId(UuidMapper.toString(saleLine.id()));
        entity.setItemId(UuidMapper.toString(saleLine.itemId()));
        entity.setFreeConceptId(UuidMapper.toString(saleLine.freeConceptId()));
        entity.setQuantity(saleLine.quantity());
        entity.setUnitPrice(saleLine.unitPrice());
        entity.setTaxId(UuidMapper.toString(saleLine.taxId()));
        return entity;
    }

    private Sale toDomain(final SaleEntity entity) {
        return new Sale(
                UuidMapper.fromString(entity.getId()),
                entity.getCode(),
                UuidMapper.fromString(entity.getEmployeeId()),
                UuidMapper.fromString(entity.getClientId()),
                UuidMapper.fromString(entity.getPaymentMethodId()),
                entity.getState(),
                entity.getCreatedOn(),
                entity.getLines().stream().map(this::toDomain).toList(),
                entity.getTotalAmount()
        );
    }

    private SaleLine toDomain(final SaleLineEntity entity) {
        return new SaleLine(
                UuidMapper.fromString(entity.getId()),
                UuidMapper.fromString(entity.getItemId()),
                UuidMapper.fromString(entity.getFreeConceptId()),
                entity.getQuantity(),
                entity.getUnitPrice(),
                UuidMapper.fromString(entity.getTaxId())
        );
    }
}
