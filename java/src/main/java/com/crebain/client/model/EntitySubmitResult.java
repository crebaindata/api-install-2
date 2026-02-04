package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of an entity submit operation.
 */
public final class EntitySubmitResult {

    private final String entityId;
    private final boolean newCompany;
    private final List<FileItem> existingFiles;
    private final boolean requestSubmitted;
    @Nullable
    private final String asyncRequestId;
    private final String requestId;

    public EntitySubmitResult(String entityId, boolean newCompany, List<FileItem> existingFiles,
                               boolean requestSubmitted, @Nullable String asyncRequestId, String requestId) {
        this.entityId = Objects.requireNonNull(entityId, "entityId cannot be null");
        this.newCompany = newCompany;
        this.existingFiles = existingFiles != null ? new ArrayList<>(existingFiles) : new ArrayList<>();
        this.requestSubmitted = requestSubmitted;
        this.asyncRequestId = asyncRequestId;
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null");
    }

    /**
     * Get the entity's UUID (created or found).
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Returns true if the entity was created by this request.
     */
    public boolean isNewCompany() {
        return newCompany;
    }

    /**
     * Get the list of files already available for this entity.
     */
    public List<FileItem> getExistingFiles() {
        return Collections.unmodifiableList(existingFiles);
    }

    /**
     * Returns true if an enrichment request was queued.
     */
    public boolean isRequestSubmitted() {
        return requestSubmitted;
    }

    /**
     * Get the ID of the async request (if submitted).
     */
    @Nullable
    public String getAsyncRequestId() {
        return asyncRequestId;
    }

    /**
     * Get the API request ID for debugging.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Create an EntitySubmitResult from a JSON response.
     */
    public static EntitySubmitResult fromJson(JsonObject data, String requestId) {
        String entityId = data.get("entity_id").getAsString();
        boolean newCompany = data.get("new_company").getAsBoolean();
        boolean requestSubmitted = data.get("request_submitted").getAsBoolean();
        String asyncRequestId = data.has("async_request_id") && !data.get("async_request_id").isJsonNull()
                ? data.get("async_request_id").getAsString() : null;

        List<FileItem> existingFiles = new ArrayList<>();
        if (data.has("existing_files") && data.get("existing_files").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("existing_files");
            for (int i = 0; i < arr.size(); i++) {
                existingFiles.add(FileItem.fromJson(arr.get(i).getAsJsonObject()));
            }
        }

        return new EntitySubmitResult(entityId, newCompany, existingFiles, requestSubmitted, asyncRequestId, requestId);
    }

    @Override
    public String toString() {
        return "EntitySubmitResult{" +
                "entityId='" + entityId + '\'' +
                ", newCompany=" + newCompany +
                ", existingFiles=" + existingFiles.size() + " files" +
                ", requestSubmitted=" + requestSubmitted +
                ", asyncRequestId='" + asyncRequestId + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
