package com.crebain.client.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Summary of an async request (for list view).
 */
public final class RequestSummary {

    private final String id;
    private final String kind;
    private final String status;
    private final Map<String, Object> payload;
    @Nullable
    private final Map<String, Object> result;
    private final String createdAt;
    private final String updatedAt;
    @Nullable
    private final String completedAt;

    public RequestSummary(String id, String kind, String status, Map<String, Object> payload,
                          @Nullable Map<String, Object> result, String createdAt, String updatedAt,
                          @Nullable String completedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.kind = Objects.requireNonNull(kind, "kind cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.payload = payload != null ? new HashMap<>(payload) : new HashMap<>();
        this.result = result != null ? new HashMap<>(result) : null;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
        this.completedAt = completedAt;
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

    public Map<String, Object> getPayload() {
        return new HashMap<>(payload);
    }

    @Nullable
    public Map<String, Object> getResult() {
        return result != null ? new HashMap<>(result) : null;
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
     * Create a RequestSummary from a JSON object.
     */
    public static RequestSummary fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String kind = json.get("kind").getAsString();
        String status = json.get("status").getAsString();
        String createdAt = json.get("created_at").getAsString();
        String updatedAt = json.get("updated_at").getAsString();
        String completedAt = json.has("completed_at") && !json.get("completed_at").isJsonNull()
                ? json.get("completed_at").getAsString() : null;

        Map<String, Object> payload = parseJsonObjectToMap(json.has("payload") ? json.getAsJsonObject("payload") : null);
        Map<String, Object> result = json.has("result") && !json.get("result").isJsonNull()
                ? parseJsonObjectToMap(json.getAsJsonObject("result")) : null;

        return new RequestSummary(id, kind, status, payload, result, createdAt, updatedAt, completedAt);
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
        return "RequestSummary{" +
                "id='" + id + '\'' +
                ", kind='" + kind + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", completedAt='" + completedAt + '\'' +
                '}';
    }
}
