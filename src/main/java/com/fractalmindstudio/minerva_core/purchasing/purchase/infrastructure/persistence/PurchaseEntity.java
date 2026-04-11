package com.fractalmindstudio.minerva_core.purchasing.purchase.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.inventory.location.infrastructure.persistence.LocationEntity;
import com.fractalmindstudio.minerva_core.purchasing.provider.infrastructure.persistence.ProviderEntity;
import com.fractalmindstudio.minerva_core.purchasing.purchase.domain.PurchaseState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Getter
@Setter
public class PurchaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime finishDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseState state;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String providerCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", referencedColumnName = "id", nullable = false)
    private ProviderEntity provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id", nullable = false)
    private LocationEntity location;

    @Column(nullable = false)
    private boolean deposit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCost;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    @OrderColumn(name = "line_position")
    private List<PurchaseLineEntity> lines = new ArrayList<>();
}
