package com.fractalmindstudio.minerva_core.model.identity;

import lombok.Data;

import java.util.UUID;

@Data
public class User {
    private UUID id = UUID.randomUUID();

    private String name;
    private String lastName;
    private String address;
}
