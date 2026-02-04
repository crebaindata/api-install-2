package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A paginated page of entities.
 */
public final class EntitiesPage {

    private final List<Entity> entities;
    @Nullable
    private final String nextCursor;
    private final String requestId;

    public EntitiesPage(List<Entity> entities, @Nullable String nextCursor, String requestId) {
        this.entities = entities != null ? new ArrayList<>(entities) : new ArrayList<>();
        this.nextCursor = nextCursor;
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null");
    }

    /**
     * Get the list of entities on this page.
     */
    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Get the cursor for the next page, or null if no more pages.
     */
    @Nullable
    public String getNextCursor() {
        return nextCursor;
    }

    /**
     * Check if there are more pages available.
     */
    public boolean hasNextPage() {
        return nextCursor != null;
    }

    /**
     * Get the API request ID for debugging.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Create an EntitiesPage from a JSON response.
     */
    public static EntitiesPage fromJson(JsonObject data, String requestId) {
        List<Entity> entities = new ArrayList<>();
        if (data.has("entities") && data.get("entities").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("entities");
            for (int i = 0; i < arr.size(); i++) {
                entities.add(Entity.fromJson(arr.get(i).getAsJsonObject()));
            }
        }

        String nextCursor = data.has("next_cursor") && !data.get("next_cursor").isJsonNull()
                ? data.get("next_cursor").getAsString() : null;

        return new EntitiesPage(entities, nextCursor, requestId);
    }

    @Override
    public String toString() {
        return "EntitiesPage{" +
                "entities=" + entities.size() + " items" +
                ", nextCursor='" + nextCursor + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
