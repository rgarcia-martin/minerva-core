package com.fractalmindstudio.minerva_core.identity.user.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();

    @Test
    void shouldGenerateStableFormatAndDifferentHashesPerInvocation() {
        final String first = passwordHasher.hash("secret");
        final String second = passwordHasher.hash("secret");

        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
        assertThat(first).isNotEqualTo(second);
        assertThat(first.split("\\$"))
                .hasSize(4)
                .allMatch(part -> !part.isBlank());
    }

    @Test
    void shouldRejectBlankRawPassword() {
        assertThatThrownBy(() -> passwordHasher.hash(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
