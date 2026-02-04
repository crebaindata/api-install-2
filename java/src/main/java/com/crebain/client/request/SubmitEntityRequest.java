package com.crebain.client.request;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request to submit an entity for enrichment.
 * <p>
 * Use the {@link #builder()} method to create instances.
 *
 * <pre>{@code
 * SubmitEntityRequest request = SubmitEntityRequest.builder()
 *     .externalEntityId("customer-123")
 *     .name("Acme Corp")
 *     .companyDescription("Global fintech company")
 *     .fields(List.of("People_control_report", "Director_graph"))
 *     .idempotencyKey("submit-acme-001")
 *     .build();
 * }</pre>
 */
public final class SubmitEntityRequest {

    @Nullable
    private final String externalEntityId;
    @Nullable
    private final String name;
    @Nullable
    private final String companyDescription;
    @Nullable
    private final Map<String, Object> metadata;
    private final boolean force;
    private final boolean adverseNewsOnly;
    @Nullable
    private final List<String> fields;
    @Nullable
    private final String idempotencyKey;

    private SubmitEntityRequest(Builder builder) {
        this.externalEntityId = builder.externalEntityId;
        this.name = builder.name;
        this.companyDescription = builder.companyDescription;
        this.metadata = builder.metadata != null ? new HashMap<>(builder.metadata) : null;
        this.force = builder.force;
        this.adverseNewsOnly = builder.adverseNewsOnly;
        this.fields = builder.fields != null ? new ArrayList<>(builder.fields) : null;
        this.idempotencyKey = builder.idempotencyKey;
    }

    @Nullable
    public String getExternalEntityId() {
        return externalEntityId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getCompanyDescription() {
        return companyDescription;
    }

    @Nullable
    public Map<String, Object> getMetadata() {
        return metadata != null ? new HashMap<>(metadata) : null;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isAdverseNewsOnly() {
        return adverseNewsOnly;
    }

    @Nullable
    public List<String> getFields() {
        return fields != null ? Collections.unmodifiableList(fields) : null;
    }

    @Nullable
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String externalEntityId;
        private String name;
        private String companyDescription;
        private Map<String, Object> metadata;
        private boolean force = false;
        private boolean adverseNewsOnly = false;
        private List<String> fields;
        private String idempotencyKey;

        /**
         * Set the external identifier (unique per org).
         */
        public Builder externalEntityId(String externalEntityId) {
            this.externalEntityId = externalEntityId;
            return this;
        }

        /**
         * Set the entity name.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set a brief description of the company (for enrichment context).
         */
        public Builder companyDescription(String companyDescription) {
            this.companyDescription = companyDescription;
            return this;
        }

        /**
         * Set additional metadata.
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Force enrichment even if files exist.
         */
        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        /**
         * Only check for adverse news.
         */
        public Builder adverseNewsOnly(boolean adverseNewsOnly) {
            this.adverseNewsOnly = adverseNewsOnly;
            return this;
        }

        /**
         * Filter returned outputs to specific field types.
         * <p>
         * Allowed values:
         * <ul>
         *   <li>Adverse_news_founder</li>
         *   <li>Adverse_news_directors</li>
         *   <li>Adverse_news_entities</li>
         *   <li>Corporate_graph_funding_vehicles</li>
         *   <li>People_control_report</li>
         *   <li>Director_graph</li>
         * </ul>
         */
        public Builder fields(List<String> fields) {
            this.fields = fields;
            return this;
        }

        /**
         * Set a unique key for idempotent request handling.
         */
        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public SubmitEntityRequest build() {
            return new SubmitEntityRequest(this);
        }
    }
}
