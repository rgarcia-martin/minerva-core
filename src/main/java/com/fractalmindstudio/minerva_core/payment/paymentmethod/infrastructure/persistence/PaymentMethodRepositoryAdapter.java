package com.fractalmindstudio.minerva_core.payment.paymentmethod.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethod;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.domain.PaymentMethodRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private final SpringDataPaymentMethodRepository springDataPaymentMethodRepository;

    public PaymentMethodRepositoryAdapter(final SpringDataPaymentMethodRepository springDataPaymentMethodRepository) {
        this.springDataPaymentMethodRepository = springDataPaymentMethodRepository;
    }

    @Override
    public PaymentMethod save(final PaymentMethod paymentMethod) {
        return toDomain(springDataPaymentMethodRepository.save(toEntity(paymentMethod)));
    }

    @Override
    public Optional<PaymentMethod> findById(final UUID id) {
        return springDataPaymentMethodRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<PaymentMethod> findAll() {
        return springDataPaymentMethodRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataPaymentMethodRepository.deleteById(UuidMapper.toString(id));
    }

    private PaymentMethodEntity toEntity(final PaymentMethod paymentMethod) {
        final PaymentMethodEntity entity = new PaymentMethodEntity();
        entity.setId(UuidMapper.toString(paymentMethod.id()));
        entity.setName(paymentMethod.name());
        entity.setType(paymentMethod.type());
        entity.setConfiguration(paymentMethod.configuration());
        return entity;
    }

    private PaymentMethod toDomain(final PaymentMethodEntity entity) {
        return new PaymentMethod(
                UuidMapper.fromString(entity.getId()),
                entity.getName(),
                entity.getType(),
                entity.getConfiguration()
        );
    }
}
