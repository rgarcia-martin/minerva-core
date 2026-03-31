package com.fractalmindstudio.minerva_core.model.location;

import lombok.Data;

import java.util.UUID;

@Data
public class Location {
    private UUID id = UUID.randomUUID();

    private String name;
    private String description;
}
