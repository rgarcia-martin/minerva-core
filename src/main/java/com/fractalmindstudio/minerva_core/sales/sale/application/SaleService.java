package com.fractalmindstudio.minerva_core.sales.sale.application;

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

    private final SaleRepository saleRepository;

    public SaleService(final SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional
    public Sale create(
            final String code,
            final UUID employeeId,
            final UUID clientId,
            final UUID paymentMethodId,
            final List<SaleLine> lines
    ) {
        return saleRepository.save(Sale.create(code, employeeId, clientId, paymentMethodId, lines));
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
