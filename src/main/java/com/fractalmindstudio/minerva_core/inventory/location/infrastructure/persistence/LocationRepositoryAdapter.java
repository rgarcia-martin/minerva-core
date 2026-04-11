package com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.inventory.location.domain.Location;
import com.fractalmindstudio.minerva_core.inventory.location.domain.LocationRepository;
import com.fractalmindstudio.minerva_core.shared.infrastructure.persistence.UuidMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LocationRepositoryAdapter implements LocationRepository {

    private final SpringDataLocationRepository springDataLocationRepository;

    public LocationRepositoryAdapter(final SpringDataLocationRepository springDataLocationRepository) {
        this.springDataLocationRepository = springDataLocationRepository;
    }

    @Override
    public Location save(final Location location) {
        return toDomain(springDataLocationRepository.save(toEntity(location)));
    }

    @Override
    public Optional<Location> findById(final UUID id) {
        return springDataLocationRepository.findById(UuidMapper.toString(id)).map(this::toDomain);
    }

    @Override
    public List<Location> findAll() {
        return springDataLocationRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(final UUID id) {
        springDataLocationRepository.deleteById(UuidMapper.toString(id));
    }

    private LocationEntity toEntity(final Location location) {
        final LocationEntity entity = new LocationEntity();
        entity.setId(UuidMapper.toString(location.id()));
        entity.setName(location.name());
        entity.setDescription(location.description());
        return entity;
    }

    private Location toDomain(final LocationEntity entity) {
        return new Location(UuidMapper.fromString(entity.getId()), entity.getName(), entity.getDescription());
    }
}
