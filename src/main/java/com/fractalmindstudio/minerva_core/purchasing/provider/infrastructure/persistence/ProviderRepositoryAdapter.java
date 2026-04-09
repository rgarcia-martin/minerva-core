package com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.purchasing.provider.domain.Provider;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.ProviderRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProviderRepositoryAdapter implements ProviderRepository {

    private final SpringDataProviderRepository springDataProviderRepository;

    public ProviderRepositoryAdapter(final SpringDataProviderRepository springDataProviderRepository) {
        this.springDataProviderRepository = springDataProviderRepository;
    }

    @Override
    public Provider save(final Provider provider) {
        return toDomain(springDataProviderRepository.save(toEntity(provider)));
    }

    @Override
    public Optional<Provider> findById(final UUID id) {
        return springDataProviderRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<Provider> findAll() {
        return springDataProviderRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataProviderRepository.deleteById(UuidMapper.toString(id));
    }

    private ProviderEntity toEntity(final Provider provider) {
        final ProviderEntity entity = new ProviderEntity();
        entity.setId(UuidMapper.toString(provider.id()));
        entity.setBusinessName(provider.businessName());
        entity.setTaxIdentifier(provider.taxIdentifier());
        entity.setAddress(provider.address());
        entity.setPhone(provider.phone());
        entity.setEmail(provider.email());
        entity.setAppliesSurcharge(provider.appliesSurcharge());
        return entity;
    }

    private Provider toDomain(final ProviderEntity entity) {
        return new Provider(
                UuidMapper.fromString(entity.getId()),
                entity.getBusinessName(),
                entity.getTaxIdentifier(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getEmail(),
                entity.isAppliesSurcharge()
        );
    }
}
