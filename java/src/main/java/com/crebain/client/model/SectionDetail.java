package com.crebain.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a section with its status and files.
 */
public final class SectionDetail {

    private final String status;
    private final List<FileItem> files;

    public SectionDetail(String status, List<FileItem> files) {
        this.status = status;
        this.files = Collections.unmodifiableList(new ArrayList<>(files));
    }

    public String getStatus() {
        return status;
    }

    public List<FileItem> getFiles() {
        return files;
    }

    public boolean isComplete() {
        return "complete".equals(status);
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    /**
     * Create a SectionDetail from a JSON object.
     */
    public static SectionDetail fromJson(JsonObject json) {
        String status = json.has("status") && !json.get("status").isJsonNull()
                ? json.get("status").getAsString() : "pending";

        List<FileItem> files = new ArrayList<>();
        if (json.has("files") && json.get("files").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("files");
            for (int i = 0; i < arr.size(); i++) {
                files.add(FileItem.fromJson(arr.get(i).getAsJsonObject()));
            }
        }

        return new SectionDetail(status, files);
    }

    @Override
    public String toString() {
        return "SectionDetail{" +
                "status='" + status + '\'' +
                ", files=" + files.size() +
                '}';
    }
}
