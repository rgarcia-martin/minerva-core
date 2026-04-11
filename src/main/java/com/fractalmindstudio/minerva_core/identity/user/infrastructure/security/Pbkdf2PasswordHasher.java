package com.fractalmindstudio.minerva_core.identity.user.infrastructure.security;

import com.fractalmindstudio.minerva_core.identity.user.application.PasswordHasher;
import com.fractalmindstudio.minerva_core.shared.domain.DomainRules;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String hash(final String rawPassword) {
        final char[] passwordChars = DomainRules.requireNonBlank(rawPassword, "user.password").toCharArray();
        final byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        try {
            final PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH);
            final SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            final byte[] hash = factory.generateSecret(spec).getEncoded();
            return "pbkdf2$" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt)
                    + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not hash password", exception);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }
}
