package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Raised when the API key lacks required permissions (403).
 * <p>
 * Check that your API key has the appropriate scope:
 * <ul>
 *   <li>{@code read} - for GET requests</li>
 *   <li>{@code write} - for POST/DELETE requests</li>
 * </ul>
 */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String code, String message, @Nullable String requestId,
                               @Nullable Integer statusCode, @Nullable String responseBody) {
        super(code, message, requestId, statusCode, responseBody);
    }
}
