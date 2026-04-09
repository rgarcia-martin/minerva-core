package com.fractalmindstudio.minerva_core.inventory.location.application;

import com.fractalmindstudio.minerva_core.inventory.location.domain.Location;
import com.fractalmindstudio.minerva_core.inventory.location.domain.LocationRepository;
import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LocationService {

    public static final String RESOURCE_NAME = "location";

    private final LocationRepository locationRepository;

    public LocationService(final LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional
    public Location create(final String name, final String description) {
        return locationRepository.save(Location.create(name, description));
    }

    public Location getById(final UUID id) {
        return locationRepository.findById(id).orElseThrow(() -> new NotFoundException(RESOURCE_NAME, id));
    }

    public List<Location> findAll() {
        return locationRepository.findAll().stream()
                .sorted(Comparator.comparing(Location::name))
                .toList();
    }

    @Transactional
    public Location update(final UUID id, final String name, final String description) {
        getById(id);
        return locationRepository.save(new Location(id, name, description));
    }

    @Transactional
    public void delete(final UUID id) {
        getById(id);
        locationRepository.deleteById(id);
    }
}
