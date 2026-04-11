package com.fractalmindstudio.minerva_core.payment.paymentmethod.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPaymentMethodRepository extends JpaRepository<PaymentMethodEntity, String> {

    List<PaymentMethodEntity> findAllByOrderByNameAsc();
}
