package com.fractalmindstudio.minerva_core.payment.paymentmethod.application;

import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethod;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodRepository;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodType;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentMethodService {

    public static final String RESOURCE_NAME = "paymentMethod";

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodService(final PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Transactional
    public PaymentMethod create(
            final String name,
            final PaymentMethodType type,
            final String configuration
    ) {
        return paymentMethodRepository.save(PaymentMethod.create(name, type, configuration));
    }

    public PaymentMethod getById(final UUID id) {
        return paymentMethodRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<PaymentMethod> findAll() {
        return paymentMethodRepository.findAll();
    }

    @Transactional
    public PaymentMethod update(
            final UUID id,
            final String name,
            final PaymentMethodType type,
            final String configuration
    ) {
        getById(id);
        return paymentMethodRepository.save(new PaymentMethod(id, name, type, configuration));
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        paymentMethodRepository.deleteById(id);
    }
}
