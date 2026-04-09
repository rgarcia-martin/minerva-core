package com.fractalmindstudio.minerva_core.sales.sale.domain;

import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Sale(
        UUID id,
        String code,
        UUID employeeId,
        UUID clientId,
        UUID paymentMethodId,
        SaleState state,
        LocalDateTime createdOn,
        List<SaleLine> lines,
        BigDecimal totalAmount
) {

    public static final String FIELD_ID = "sale.id";
    public static final String FIELD_CODE = "sale.code";
    public static final String FIELD_EMPLOYEE_ID = "sale.employeeId";
    public static final String FIELD_PAYMENT_METHOD_ID = "sale.paymentMethodId";
    public static final String FIELD_LINES = "sale.lines";
    public static final String FIELD_TOTAL_AMOUNT = "sale.totalAmount";

    public Sale {
        DomainRules.requireNonNull(id, FIELD_ID);
        code = DomainRules.requireNonBlank(code, FIELD_CODE);
        DomainRules.requireNonNull(employeeId, FIELD_EMPLOYEE_ID);
        DomainRules.requireNonNull(paymentMethodId, FIELD_PAYMENT_METHOD_ID);
        state = state == null ? SaleState.NEW : state;
        DomainRules.requireNonNull(lines, FIELD_LINES);
        lines = List.copyOf(lines);
        DomainRules.requirePositiveOrZero(totalAmount, FIELD_TOTAL_AMOUNT);
        totalAmount = DomainRules.scaleMoney(totalAmount);
    }

    public static Sale create(
            final String code,
            final UUID employeeId,
            final UUID clientId,
            final UUID paymentMethodId,
            final List<SaleLine> lines
    ) {
        DomainRules.requireNonNull(lines, FIELD_LINES);
        final BigDecimal total = lines.stream()
                .map(SaleLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Sale(
                UUID.randomUUID(),
                code,
                employeeId,
                clientId,
                paymentMethodId,
                SaleState.NEW,
                LocalDateTime.now(),
                lines,
                total
        );
    }

    public Sale confirm() {
        return new Sale(id, code, employeeId, clientId, paymentMethodId, SaleState.CONFIRMED, createdOn, lines, totalAmount);
    }

    public Sale cancel() {
        return new Sale(id, code, employeeId, clientId, paymentMethodId, SaleState.CANCELLED, createdOn, lines, totalAmount);
    }
}
