package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Information about an async request.
 */
public final class RequestInfo {

    private final String id;
    private final String kind;
    private final String status;
    private final Map<String, Object> payload;
    @Nullable
    private final Map<String, Object> result;
    private final List<FileItem> files;
    private final String createdAt;
    private final String updatedAt;
    @Nullable
    private final String completedAt;
    private final String requestId;

    public RequestInfo(String id, String kind, String status, Map<String, Object> payload,
                       @Nullable Map<String, Object> result, List<FileItem> files,
                       String createdAt, String updatedAt, @Nullable String completedAt, String requestId) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.kind = Objects.requireNonNull(kind, "kind cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.payload = payload != null ? new HashMap<>(payload) : new HashMap<>();
        this.result = result != null ? new HashMap<>(result) : null;
        this.files = files != null ? new ArrayList<>(files) : new ArrayList<>();
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
        this.completedAt = completedAt;
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null");
    }

    public String getId() {
        return id;
    }

    /**
     * Get request kind (entity_enrich, url_ingest).
     */
    public String getKind() {
        return kind;
    }

    /**
     * Get request status (submitted, processing, complete, failed).
     */
    public String getStatus() {
        return status;
    }

    /**
     * Check if the request is complete.
     */
    public boolean isComplete() {
        return "complete".equals(status);
    }

    /**
     * Check if the request failed.
     */
    public boolean isFailed() {
        return "failed".equals(status);
    }

    public Map<String, Object> getPayload() {
        return new HashMap<>(payload);
    }

    @Nullable
    public Map<String, Object> getResult() {
        return result != null ? new HashMap<>(result) : null;
    }

    /**
     * Get associated files with signed URLs (if complete).
     */
    public List<FileItem> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    @Nullable
    public String getCompletedAt() {
        return completedAt;
    }

    /**
     * Get the API request ID for debugging.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Create a RequestInfo from a JSON response.
     */
    public static RequestInfo fromJson(JsonObject data, String requestId) {
        String id = data.get("id").getAsString();
        String kind = data.get("kind").getAsString();
        String status = data.get("status").getAsString();
        String createdAt = data.get("created_at").getAsString();
        String updatedAt = data.get("updated_at").getAsString();
        String completedAt = data.has("completed_at") && !data.get("completed_at").isJsonNull()
                ? data.get("completed_at").getAsString() : null;

        Map<String, Object> payload = parseJsonObjectToMap(
                data.has("payload") && data.get("payload").isJsonObject() ? data.getAsJsonObject("payload") : null);
        Map<String, Object> result = data.has("result") && !data.get("result").isJsonNull() && data.get("result").isJsonObject()
                ? parseJsonObjectToMap(data.getAsJsonObject("result")) : null;

        List<FileItem> files = new ArrayList<>();
        if (data.has("files") && data.get("files").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("files");
            for (int i = 0; i < arr.size(); i++) {
                files.add(FileItem.fromJson(arr.get(i).getAsJsonObject()));
            }
        }

        return new RequestInfo(id, kind, status, payload, result, files, createdAt, updatedAt, completedAt, requestId);
    }

    private static Map<String, Object> parseJsonObjectToMap(@Nullable JsonObject obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;

        for (String key : obj.keySet()) {
            var element = obj.get(key);
            if (element.isJsonPrimitive()) {
                var prim = element.getAsJsonPrimitive();
                if (prim.isString()) {
                    map.put(key, prim.getAsString());
                } else if (prim.isNumber()) {
                    map.put(key, prim.getAsNumber());
                } else if (prim.isBoolean()) {
                    map.put(key, prim.getAsBoolean());
                }
            } else if (element.isJsonArray()) {
                map.put(key, element.getAsJsonArray().toString());
            } else if (element.isJsonObject()) {
                map.put(key, element.getAsJsonObject().toString());
            }
        }
        return map;
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "id='" + id + '\'' +
                ", kind='" + kind + '\'' +
                ", status='" + status + '\'' +
                ", files=" + files.size() + " files" +
                ", createdAt='" + createdAt + '\'' +
                ", completedAt='" + completedAt + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
