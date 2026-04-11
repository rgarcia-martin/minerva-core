package com.fractalmindstudio.minerva_core.identity.user.application;

public interface PasswordHasher {

    String hash(String rawPassword);
}
