package com.crebain.client.request;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Request to submit a person for adverse news check.
 * <p>
 * Use the {@link #builder(String)} method to create instances.
 *
 * <pre>{@code
 * SubmitPersonRequest request = SubmitPersonRequest.builder("John Smith")
 *     .companyName("Acme Corp")
 *     .entityId("entity-uuid-123")
 *     .idempotencyKey("person-check-001")
 *     .build();
 * }</pre>
 */
public final class SubmitPersonRequest {

    private final String name;
    @Nullable
    private final String companyName;
    @Nullable
    private final String entityId;
    @Nullable
    private final String idempotencyKey;

    private SubmitPersonRequest(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "name is required");
        this.companyName = builder.companyName;
        this.entityId = builder.entityId;
        this.idempotencyKey = builder.idempotencyKey;
    }

    /**
     * Get the person's full name (required).
     */
    public String getName() {
        return name;
    }

    /**
     * Get the optional company name for context.
     */
    @Nullable
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Get the optional entity ID to associate the person with.
     */
    @Nullable
    public String getEntityId() {
        return entityId;
    }

    /**
     * Get the idempotency key.
     */
    @Nullable
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * Create a builder with the required person name.
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String companyName;
        private String entityId;
        private String idempotencyKey;

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "name is required");
        }

        /**
         * Set the optional company name for context.
         */
        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        /**
         * Set the optional entity ID to associate the person with.
         */
        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        /**
         * Set a unique key for idempotent request handling.
         */
        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public SubmitPersonRequest build() {
            return new SubmitPersonRequest(this);
        }
    }
}
