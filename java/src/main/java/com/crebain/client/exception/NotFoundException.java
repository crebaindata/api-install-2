package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Raised when a requested resource is not found (404).
 */
public class NotFoundException extends ApiException {

    public NotFoundException(String code, String message, @Nullable String requestId,
                              @Nullable Integer statusCode, @Nullable String responseBody) {
        super(code, message, requestId, statusCode, responseBody);
    }
}
