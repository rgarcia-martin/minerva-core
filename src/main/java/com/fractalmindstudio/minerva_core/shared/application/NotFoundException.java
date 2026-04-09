package com.fractalmindstudio.minerva_core.shared.application;

import java.util.UUID;

public class NotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "%s with id %s was not found";

    public NotFoundException(final String resourceName, final UUID id) {
        super(MESSAGE_TEMPLATE.formatted(resourceName, id));
    }
}
