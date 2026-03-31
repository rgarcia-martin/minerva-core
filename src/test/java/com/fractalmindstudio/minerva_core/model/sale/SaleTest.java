package com.fractalmindstudio.minerva_core.model.sale;

import com.fractalmindstudio.minerva_core.model.identity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SaleTest {

    private static final UUID SALE_ID = UUID.fromString("11223344-1234-1234-1234-123456789012");
    private static final UUID USER_ID = UUID.fromString("22334455-1234-1234-1234-123456789012");
    private static final LocalDateTime CREATED_ON = LocalDateTime.of(2026, 3, 31, 18, 30, 0);
    private static final String CODE = "SALE-001";
    private static final String UPDATED_CODE = "SALE-002";
    private static final String USER_NAME = "Grace";

    @Test
    void shouldCreateSaleWithDefaultValuesAndGeneratedId() {
        Sale sale = new Sale();

        assertThat(sale.getId()).isNotNull();
        assertThat(sale.getUser()).isNull();
        assertThat(sale.getCreatedOn()).isNull();
        assertThat(sale.getCode()).isNull();
    }

    @Test
    void shouldStoreAssignedValues() {
        User user = createUser();
        Sale sale = new Sale();

        sale.setUser(user);
        sale.setCreatedOn(CREATED_ON);
        sale.setCode(CODE);

        assertThat(sale.getUser()).isEqualTo(user);
        assertThat(sale.getCreatedOn()).isEqualTo(CREATED_ON);
        assertThat(sale.getCode()).isEqualTo(CODE);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnState() {
        User user = createUser();
        Sale first = createSale(user, CODE);
        Sale second = createSale(user, CODE);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);

        second.setCode(UPDATED_CODE);

        assertThat(first).isNotEqualTo(second);
    }

    private Sale createSale(User user, String code) {
        Sale sale = new Sale();
        sale.setId(SALE_ID);
        sale.setUser(user);
        sale.setCreatedOn(CREATED_ON);
        sale.setCode(code);
        return sale;
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setName(USER_NAME);
        return user;
    }
}
