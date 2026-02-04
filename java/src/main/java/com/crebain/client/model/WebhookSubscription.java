package com.crebain.client.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a webhook subscription.
 */
public final class WebhookSubscription {

    private final String id;
    private final String url;
    private final boolean enabled;
    private final String createdAt;
    @Nullable
    private final String updatedAt;

    public WebhookSubscription(String id, String url, boolean enabled, String createdAt,
                                @Nullable String updatedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.url = Objects.requireNonNull(url, "url cannot be null");
        this.enabled = enabled;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = updatedAt;
    }

    /**
     * Get the unique webhook subscription ID (UUID).
     */
    public String getId() {
        return id;
    }

    /**
     * Get the webhook endpoint URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Check if the webhook is active.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the ISO 8601 timestamp of creation.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the ISO 8601 timestamp of last update (optional).
     */
    @Nullable
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Create a WebhookSubscription from a JSON object.
     */
    public static WebhookSubscription fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String url = json.get("url").getAsString();
        boolean enabled = json.get("enabled").getAsBoolean();
        String createdAt = json.get("created_at").getAsString();
        String updatedAt = json.has("updated_at") && !json.get("updated_at").isJsonNull()
                ? json.get("updated_at").getAsString() : null;

        return new WebhookSubscription(id, url, enabled, createdAt, updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebhookSubscription that = (WebhookSubscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WebhookSubscription{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", enabled=" + enabled +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
