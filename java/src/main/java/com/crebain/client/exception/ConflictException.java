package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Raised on idempotency conflicts or resource state conflicts (409).
 * <p>
 * This can occur when:
 * <ul>
 *   <li>An idempotency key is reused with a different request body</li>
 *   <li>A resource is already in a conflicting state</li>
 * </ul>
 */
public class ConflictException extends ApiException {

    public ConflictException(String code, String message, @Nullable String requestId,
                              @Nullable Integer statusCode, @Nullable String responseBody) {
        super(code, message, requestId, statusCode, responseBody);
    }
}
