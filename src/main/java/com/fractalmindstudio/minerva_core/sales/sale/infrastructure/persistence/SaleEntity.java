package com.fractalmindstudio.minerva_core.sales.sale.infrastructure.persistence;

import com.fractalmindstudio.minerva_core.identity.user.infrastructure.persistence.UserEntity;
import com.fractalmindstudio.minerva_core.payment.paymentmethod.infrastructure.persistence.PaymentMethodEntity;
import com.fractalmindstudio.minerva_core.sales.sale.domain.SaleState;
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
@Table(name = "sales")
@Getter
@Setter
public class SaleEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", nullable = false)
    private UserEntity employee;

    @Column(length = 36)
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id", nullable = false)
    private PaymentMethodEntity paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleState state;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @OrderColumn(name = "line_position")
    private List<SaleLineEntity> lines = new ArrayList<>();
}
