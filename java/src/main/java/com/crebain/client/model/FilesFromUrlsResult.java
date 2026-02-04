package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of a files-from-urls operation.
 */
public final class FilesFromUrlsResult {

    private final List<FileItem> files;
    private final List<String> missing;
    private final boolean requestSubmitted;
    @Nullable
    private final String asyncRequestId;
    private final String requestId;

    public FilesFromUrlsResult(List<FileItem> files, List<String> missing, boolean requestSubmitted,
                                @Nullable String asyncRequestId, String requestId) {
        this.files = files != null ? new ArrayList<>(files) : new ArrayList<>();
        this.missing = missing != null ? new ArrayList<>(missing) : new ArrayList<>();
        this.requestSubmitted = requestSubmitted;
        this.asyncRequestId = asyncRequestId;
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null");
    }

    /**
     * Get the list of files already available with signed URLs.
     */
    public List<FileItem> getFiles() {
        return Collections.unmodifiableList(files);
    }

    /**
     * Get the list of URLs that need to be ingested.
     */
    public List<String> getMissing() {
        return Collections.unmodifiableList(missing);
    }

    /**
     * Returns true if an ingestion request was queued.
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
     * Create a FilesFromUrlsResult from a JSON response.
     */
    public static FilesFromUrlsResult fromJson(JsonObject data, String requestId) {
        List<FileItem> files = new ArrayList<>();
        if (data.has("files") && data.get("files").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("files");
            for (int i = 0; i < arr.size(); i++) {
                files.add(FileItem.fromJson(arr.get(i).getAsJsonObject()));
            }
        }

        List<String> missing = new ArrayList<>();
        if (data.has("missing") && data.get("missing").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("missing");
            for (int i = 0; i < arr.size(); i++) {
                missing.add(arr.get(i).getAsString());
            }
        }

        boolean requestSubmitted = data.get("request_submitted").getAsBoolean();
        String asyncRequestId = data.has("async_request_id") && !data.get("async_request_id").isJsonNull()
                ? data.get("async_request_id").getAsString() : null;

        return new FilesFromUrlsResult(files, missing, requestSubmitted, asyncRequestId, requestId);
    }

    @Override
    public String toString() {
        return "FilesFromUrlsResult{" +
                "files=" + files.size() + " files" +
                ", missing=" + missing.size() + " URLs" +
                ", requestSubmitted=" + requestSubmitted +
                ", asyncRequestId='" + asyncRequestId + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
