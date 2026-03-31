package com.fractalmindstudio.minerva_core.model.tax;

import lombok.Data;

import java.util.UUID;

@Data
public class Tax {

    private UUID id = UUID.randomUUID();

    private String description;
    private Float tax;

    private Tax specialTax;
}
