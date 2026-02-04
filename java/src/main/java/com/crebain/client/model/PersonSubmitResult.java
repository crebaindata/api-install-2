package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of a person submit operation (adverse news for founders).
 */
public final class PersonSubmitResult {

    private final String personName;
    @Nullable
    private final String companyName;
    @Nullable
    private final String entityId;
    private final List<FileItem> existingFiles;
    private final boolean requestSubmitted;
    @Nullable
    private final String asyncRequestId;
    private final String requestId;

    public PersonSubmitResult(String personName, @Nullable String companyName, @Nullable String entityId,
                               List<FileItem> existingFiles, boolean requestSubmitted,
                               @Nullable String asyncRequestId, String requestId) {
        this.personName = Objects.requireNonNull(personName, "personName cannot be null");
        this.companyName = companyName;
        this.entityId = entityId;
        this.existingFiles = existingFiles != null ? new ArrayList<>(existingFiles) : new ArrayList<>();
        this.requestSubmitted = requestSubmitted;
        this.asyncRequestId = asyncRequestId;
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null");
    }

    /**
     * Get the person's name that was submitted.
     */
    public String getPersonName() {
        return personName;
    }

    /**
     * Get the company name (if provided).
     */
    @Nullable
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Get the entity ID (if provided).
     */
    @Nullable
    public String getEntityId() {
        return entityId;
    }

    /**
     * Get the list of files available (Adverse_news_founder outputs).
     */
    public List<FileItem> getExistingFiles() {
        return Collections.unmodifiableList(existingFiles);
    }

    /**
     * Returns true if an async request was queued.
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
     * Create a PersonSubmitResult from a JSON response.
     */
    public static PersonSubmitResult fromJson(JsonObject data, String requestId) {
        String personName = data.get("person_name").getAsString();
        String companyName = data.has("company_name") && !data.get("company_name").isJsonNull()
                ? data.get("company_name").getAsString() : null;
        String entityId = data.has("entity_id") && !data.get("entity_id").isJsonNull()
                ? data.get("entity_id").getAsString() : null;
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

        return new PersonSubmitResult(personName, companyName, entityId, existingFiles,
                requestSubmitted, asyncRequestId, requestId);
    }

    @Override
    public String toString() {
        return "PersonSubmitResult{" +
                "personName='" + personName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", entityId='" + entityId + '\'' +
                ", existingFiles=" + existingFiles.size() + " files" +
                ", requestSubmitted=" + requestSubmitted +
                ", asyncRequestId='" + asyncRequestId + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
