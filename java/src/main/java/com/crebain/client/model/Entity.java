package com.crebain.client.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an entity in the system.
 */
public final class Entity {

    private final String id;
    @Nullable
    private final String externalEntityId;
    @Nullable
    private final String name;
    private final Map<String, Object> metadata;
    private final String createdAt;
    private final String updatedAt;

    public Entity(String id, @Nullable String externalEntityId, @Nullable String name,
                  Map<String, Object> metadata, String createdAt, String updatedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.externalEntityId = externalEntityId;
        this.name = name;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    public String getId() {
        return id;
    }

    @Nullable
    public String getExternalEntityId() {
        return externalEntityId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Create an Entity from a JSON object.
     */
    public static Entity fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String externalEntityId = json.has("external_entity_id") && !json.get("external_entity_id").isJsonNull()
                ? json.get("external_entity_id").getAsString() : null;
        String name = json.has("name") && !json.get("name").isJsonNull()
                ? json.get("name").getAsString() : null;

        Map<String, Object> metadata = new HashMap<>();
        if (json.has("metadata") && json.get("metadata").isJsonObject()) {
            JsonObject metaObj = json.getAsJsonObject("metadata");
            for (String key : metaObj.keySet()) {
                if (metaObj.get(key).isJsonPrimitive()) {
                    var prim = metaObj.get(key).getAsJsonPrimitive();
                    if (prim.isString()) {
                        metadata.put(key, prim.getAsString());
                    } else if (prim.isNumber()) {
                        metadata.put(key, prim.getAsNumber());
                    } else if (prim.isBoolean()) {
                        metadata.put(key, prim.getAsBoolean());
                    }
                }
            }
        }

        String createdAt = json.get("created_at").getAsString();
        String updatedAt = json.get("updated_at").getAsString();

        return new Entity(id, externalEntityId, name, metadata, createdAt, updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Entity{" +
                "id='" + id + '\'' +
                ", externalEntityId='" + externalEntityId + '\'' +
                ", name='" + name + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
