package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Raised when request validation fails (422).
 * <p>
 * Check the error message for details on what validation failed.
 */
public class ValidationException extends ApiException {

    public ValidationException(String code, String message, @Nullable String requestId,
                                @Nullable Integer statusCode, @Nullable String responseBody) {
        super(code, message, requestId, statusCode, responseBody);
    }
}
