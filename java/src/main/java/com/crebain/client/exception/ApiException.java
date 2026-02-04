package com.crebain.client.exception;

import org.jetbrains.annotations.Nullable;

/**
 * Base exception for all Crebain API errors.
 * <p>
 * All API errors include:
 * <ul>
 *   <li>{@code code} - Machine-readable error code (e.g., 'UNAUTHORIZED', 'RATE_LIMITED')</li>
 *   <li>{@code message} - Human-readable error description</li>
 *   <li>{@code requestId} - Unique request identifier for debugging</li>
 *   <li>{@code statusCode} - HTTP status code</li>
 * </ul>
 */
public class ApiException extends RuntimeException {

    private final String code;
    private final String errorMessage;
    @Nullable
    private final String requestId;
    @Nullable
    private final Integer statusCode;
    @Nullable
    private final String responseBody;

    public ApiException(String code, String message, @Nullable String requestId,
                        @Nullable Integer statusCode, @Nullable String responseBody) {
        super(String.format("[%s] %s (request_id=%s)", code, message, requestId));
        this.code = code;
        this.errorMessage = message;
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Nullable
    public String getRequestId() {
        return requestId;
    }

    @Nullable
    public Integer getStatusCode() {
        return statusCode;
    }

    @Nullable
    public String getResponseBody() {
        return responseBody;
    }

    @Override
    public String toString() {
        return String.format("%s(code=%s, message=%s, requestId=%s, statusCode=%s)",
                getClass().getSimpleName(), code, errorMessage, requestId, statusCode);
    }
}
