package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Raised when authentication fails (401).
 * <p>
 * This can occur when:
 * <ul>
 *   <li>API key is missing</li>
 *   <li>API key is invalid</li>
 *   <li>API key has been revoked</li>
 *   <li>API key has expired</li>
 * </ul>
 */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String code, String message, @Nullable String requestId,
                                  @Nullable Integer statusCode, @Nullable String responseBody) {
        super(code, message, requestId, statusCode, responseBody);
    }
}
