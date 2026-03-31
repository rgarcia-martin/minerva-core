package com.fractalmindstudio.minerva_core.model.sale;

import com.fractalmindstudio.minerva_core.model.identity.User;
import lombok.Data;

import java.util.UUID;

@Data
public class Sale {
    private UUID id = UUID.randomUUID();

    private User user;

}
