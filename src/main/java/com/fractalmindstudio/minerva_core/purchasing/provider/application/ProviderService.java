package com.fractalmindstudio.minerva_core.purchasing.provider.application;

import com.fractalmindstudio.minerva_core.purchasing.provider.domain.Provider;
import com.fractalmindstudio.minerva_core.purchasing.provider.domain.ProviderRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProviderService {

    public static final String RESOURCE_NAME = "provider";

    private final ProviderRepository providerRepository;

    public ProviderService(final ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Transactional
    public Provider create(
            final String businessName,
            final String taxIdentifier,
            final String address,
            final String phone,
            final String email,
            final boolean appliesSurcharge
    ) {
        return providerRepository.save(Provider.create(businessName, taxIdentifier, address, phone, email, appliesSurcharge));
    }

    public Provider getById(final UUID id) {
        return providerRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Provider> findAll() {
        return providerRepository.findAll();
    }

    @Transactional
    public Provider update(
            final UUID id,
            final String businessName,
            final String taxIdentifier,
            final String address,
            final String phone,
            final String email,
            final boolean appliesSurcharge
    ) {
        getById(id);
        return providerRepository.save(new Provider(id, businessName, taxIdentifier, address, phone, email, appliesSurcharge));
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        providerRepository.deleteById(id);
    }
}
