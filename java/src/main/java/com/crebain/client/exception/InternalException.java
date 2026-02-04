package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Raised on server-side errors (500).
 * <p>
 * These errors are retryable. Implement exponential backoff with a maximum
 * of 3 retry attempts.
 */
public class InternalException extends ApiException {

    public InternalException(String code, String message, @Nullable String requestId,
                              @Nullable Integer statusCode, @Nullable String responseBody) {
        super(code, message, requestId, statusCode, responseBody);
    }
}
