package com.crebain.client.request;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Request to get files from URLs or trigger ingestion for missing ones.
 * <p>
 * Use the {@link #builder(List)} method to create instances.
 *
 * <pre>{@code
 * FilesFromUrlsRequest request = FilesFromUrlsRequest.builder(
 *         List.of("https://example.com/doc1.pdf", "https://example.com/doc2.pdf"))
 *     .entityId("entity-uuid")
 *     .force(false)
 *     .idempotencyKey("ingest-batch-001")
 *     .build();
 * }</pre>
 */
public final class FilesFromUrlsRequest {

    private final List<String> urlList;
    @Nullable
    private final String entityId;
    private final boolean force;
    @Nullable
    private final String idempotencyKey;

    private FilesFromUrlsRequest(Builder builder) {
        this.urlList = new ArrayList<>(builder.urlList);
        this.entityId = builder.entityId;
        this.force = builder.force;
        this.idempotencyKey = builder.idempotencyKey;
    }

    /**
     * Get the list of URLs to fetch files from (max 50).
     */
    public List<String> getUrlList() {
        return Collections.unmodifiableList(urlList);
    }

    /**
     * Get the optional entity to associate files with.
     */
    @Nullable
    public String getEntityId() {
        return entityId;
    }

    /**
     * Check if force re-ingestion is enabled.
     */
    public boolean isForce() {
        return force;
    }

    /**
     * Get the idempotency key.
     */
    @Nullable
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * Create a builder with the required URL list.
     */
    public static Builder builder(List<String> urlList) {
        return new Builder(urlList);
    }

    public static class Builder {
        private final List<String> urlList;
        private String entityId;
        private boolean force = false;
        private String idempotencyKey;

        private Builder(List<String> urlList) {
            Objects.requireNonNull(urlList, "urlList is required");
            if (urlList.isEmpty()) {
                throw new IllegalArgumentException("urlList cannot be empty");
            }
            if (urlList.size() > 50) {
                throw new IllegalArgumentException("urlList cannot exceed 50 URLs");
            }
            this.urlList = new ArrayList<>(urlList);
        }

        /**
         * Set the optional entity to associate files with.
         */
        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        /**
         * Force re-ingestion of all URLs.
         */
        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        /**
         * Set a unique key for idempotent request handling.
         */
        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public FilesFromUrlsRequest build() {
            return new FilesFromUrlsRequest(this);
        }
    }
}
