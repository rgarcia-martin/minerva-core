package com.fractalmindstudio.minerva_core.model.provider;

import lombok.Data;

import java.util.UUID;

@Data
public class Provider {
    private UUID id = UUID.randomUUID();

    private String name;

    private Boolean specialTax = false;

}
