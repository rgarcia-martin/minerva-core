package com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "providers")
@Getter
@Setter
public class ProviderEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String taxIdentifier;

    @Column(length = 4000)
    private String address;

    private String phone;

    private String email;

    @Column(nullable = false)
    private boolean appliesSurcharge;
}
