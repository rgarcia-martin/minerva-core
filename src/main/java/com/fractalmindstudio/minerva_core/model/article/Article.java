package com.fractalmindstudio.minerva_core.model.article;

import com.fractalmindstudio.minerva_core.model.tax.Tax;
import lombok.Data;

import java.util.UUID;

@Data
public class Article {
    private UUID id = UUID.randomUUID();

    private String name;
    private String code;

    private String image;
    private String description;

    private Tax tax;
    private Float priceBase;
    private Float pricePVP;

    private Boolean couldHaveChildren = false;
    private Integer numberOfChildren = 0;

    private Article parent;
    private Article child;
}
