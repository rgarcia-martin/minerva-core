package com.fractalmindstudio.minerva_core.payment.paymentmethod.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository {

    PaymentMethod save(PaymentMethod paymentMethod);

    Optional<PaymentMethod> findById(UUID id);

    List<PaymentMethod> findAll();

    void deleteById(UUID id);
}
