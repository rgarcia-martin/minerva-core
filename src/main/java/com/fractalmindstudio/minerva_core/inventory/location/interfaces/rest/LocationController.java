package com.fractalmindstudio.minerva_core.inventory.location.interfaces.rest;

import com.fractalmindstudio.minerva_core.inventory.location.application.LocationService;
import com.fractalmindstudio.minerva_core.inventory.location.domain.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(LocationController.BASE_PATH)
@Validated
public class LocationController {

    public static final String BASE_PATH = "/api/v1/locations";

    private final LocationService locationService;

    public LocationController(final LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody final UpsertLocationRequest request) {
        final Location location = locationService.create(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(location));
    }

    @GetMapping
    public List<LocationResponse> findAll() {
        return locationService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{locationId}")
    public LocationResponse getById(@PathVariable final UUID locationId) {
        return toResponse(locationService.getById(locationId));
    }

    @PutMapping("/{locationId}")
    public LocationResponse update(
            @PathVariable final UUID locationId,
            @Valid @RequestBody final UpsertLocationRequest request
    ) {
        final Location location = locationService.update(locationId, request.name(), request.description());
        return toResponse(location);
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> delete(@PathVariable final UUID locationId) {
        locationService.delete(locationId);
        return ResponseEntity.noContent().build();
    }

    private LocationResponse toResponse(final Location location) {
        return new LocationResponse(location.id(), location.name(), location.description());
    }

    public record UpsertLocationRequest(
            @NotBlank String name,
            String description
    ) {
    }

    public record LocationResponse(
            UUID id,
            String name,
            String description
    ) {
    }
}
