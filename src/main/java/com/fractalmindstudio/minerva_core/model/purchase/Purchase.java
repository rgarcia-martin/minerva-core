package com.fractalmindstudio.minerva_core.model.purchase;

import com.fractalmindstudio.minerva_core.model.provider.Provider;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Purchase {
    private UUID id = UUID.randomUUID();

    private LocalDateTime createdOn;
    private LocalDateTime finishDate;
    private PurchaseState state = PurchaseState.NEW;

    private String code;
    private Provider provider;

    private List<PurchaseItem> purchaseItemList = new ArrayList<>();

    private Float totalCost;
}
