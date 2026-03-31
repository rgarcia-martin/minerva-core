package com.fractalmindstudio.minerva_core.model.item;

import com.fractalmindstudio.minerva_core.model.article.Article;
import com.fractalmindstudio.minerva_core.model.location.Location;
import com.fractalmindstudio.minerva_core.model.tax.Tax;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Item {
    private UUID id = UUID.randomUUID();
    private Article article;

    private ItemStatus itemStatus;
    private Item parent;

    private Boolean hasChildren = false;
    private List<Item> children = new ArrayList<>();

    private Float cost;
    private Tax buyTax;
    private Tax specialBuyTax;

    private Location location;
}
