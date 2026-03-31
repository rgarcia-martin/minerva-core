package com.fractalmindstudio.minerva_core.model.purchase;

import com.fractalmindstudio.minerva_core.model.item.Item;
import com.fractalmindstudio.minerva_core.model.tax.Tax;
import lombok.Data;

import java.util.UUID;

@Data
public class PurchaseItem {
    private UUID id = UUID.randomUUID();

    private Item item;
    private Purchase purchase;

    private Tax tax;
    private Tax specialTax;

    private Float buyPrice;
}
