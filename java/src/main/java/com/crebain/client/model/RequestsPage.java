package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A paginated page of requests.
 */
public final class RequestsPage {

    private final List<RequestSummary> requests;
    @Nullable
    private final String nextCursor;
    private final String requestId;

    public RequestsPage(List<RequestSummary> requests, @Nullable String nextCursor, String requestId) {
        this.requests = requests != null ? new ArrayList<>(requests) : new ArrayList<>();
        this.nextCursor = nextCursor;
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null");
    }

    /**
     * Get the list of requests on this page.
     */
    public List<RequestSummary> getRequests() {
        return Collections.unmodifiableList(requests);
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
     * Create a RequestsPage from a JSON response.
     */
    public static RequestsPage fromJson(JsonObject data, String requestId) {
        List<RequestSummary> requests = new ArrayList<>();
        if (data.has("requests") && data.get("requests").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("requests");
            for (int i = 0; i < arr.size(); i++) {
                requests.add(RequestSummary.fromJson(arr.get(i).getAsJsonObject()));
            }
        }

        String nextCursor = data.has("next_cursor") && !data.get("next_cursor").isJsonNull()
                ? data.get("next_cursor").getAsString() : null;

        return new RequestsPage(requests, nextCursor, requestId);
    }

    @Override
    public String toString() {
        return "RequestsPage{" +
                "requests=" + requests.size() + " items" +
                ", nextCursor='" + nextCursor + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
