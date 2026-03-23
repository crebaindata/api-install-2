package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Response from GET /v1/requests/{id}/sections.
 * Contains files grouped by section with completion status.
 */
public final class SectionsResponse {

    private final String requestId;
    private final String status;
    private final Map<String, SectionDetail> sections;
    private final List<String> sectionsCompleted;
    private final List<String> sectionsPending;
    @Nullable
    private final String apiRequestId;

    public SectionsResponse(String requestId, String status, Map<String, SectionDetail> sections,
                            List<String> sectionsCompleted, List<String> sectionsPending,
                            @Nullable String apiRequestId) {
        this.requestId = Objects.requireNonNull(requestId, "requestId cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.sections = Collections.unmodifiableMap(new LinkedHashMap<>(sections));
        this.sectionsCompleted = Collections.unmodifiableList(new ArrayList<>(sectionsCompleted));
        this.sectionsPending = Collections.unmodifiableList(new ArrayList<>(sectionsPending));
        this.apiRequestId = apiRequestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, SectionDetail> getSections() {
        return sections;
    }

    public List<String> getSectionsCompleted() {
        return sectionsCompleted;
    }

    public List<String> getSectionsPending() {
        return sectionsPending;
    }

    @Nullable
    public String getApiRequestId() {
        return apiRequestId;
    }

    /**
     * Create a SectionsResponse from a JSON data object and API request ID.
     */
    public static SectionsResponse fromJson(JsonObject data, String apiRequestId) {
        String requestId = data.get("request_id").getAsString();
        String status = data.get("status").getAsString();

        Map<String, SectionDetail> sections = new LinkedHashMap<>();
        if (data.has("sections") && data.get("sections").isJsonObject()) {
            JsonObject sectionsObj = data.getAsJsonObject("sections");
            for (String key : sectionsObj.keySet()) {
                sections.put(key, SectionDetail.fromJson(sectionsObj.getAsJsonObject(key)));
            }
        }

        List<String> sectionsCompleted = new ArrayList<>();
        if (data.has("sections_completed") && data.get("sections_completed").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("sections_completed");
            for (int i = 0; i < arr.size(); i++) {
                sectionsCompleted.add(arr.get(i).getAsString());
            }
        }

        List<String> sectionsPending = new ArrayList<>();
        if (data.has("sections_pending") && data.get("sections_pending").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("sections_pending");
            for (int i = 0; i < arr.size(); i++) {
                sectionsPending.add(arr.get(i).getAsString());
            }
        }

        return new SectionsResponse(requestId, status, sections, sectionsCompleted, sectionsPending, apiRequestId);
    }

    @Override
    public String toString() {
        return "SectionsResponse{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", sections=" + sections.size() +
                ", completed=" + sectionsCompleted.size() +
                ", pending=" + sectionsPending.size() +
                '}';
    }
}
