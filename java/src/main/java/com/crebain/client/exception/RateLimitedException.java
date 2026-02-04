package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Raised when rate limit is exceeded (429).
 * <p>
 * The API allows 60 requests per minute. When this limit is exceeded,
 * implement exponential backoff before retrying.
 */
public class RateLimitedException extends ApiException {

    public RateLimitedException(String code, String message, @Nullable String requestId,
                                 @Nullable Integer statusCode, @Nullable String responseBody) {
        super(code, message, requestId, statusCode, responseBody);
    }
}
