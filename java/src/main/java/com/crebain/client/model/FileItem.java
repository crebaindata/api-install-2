package com.crebain.client.model;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a file in the system.
 */
public final class FileItem {

    private final String fileId;
    private final String filename;
    @Nullable
    private final String mimeType;
    @Nullable
    private final Long bytes;
    @Nullable
    private final String sourceUrl;
    @Nullable
    private final String signedUrl;
    private final String createdAt;
    @Nullable
    private final String requestId;

    public FileItem(String fileId, String filename, @Nullable String mimeType,
                    @Nullable Long bytes, @Nullable String sourceUrl, @Nullable String signedUrl,
                    String createdAt, @Nullable String requestId) {
        this.fileId = Objects.requireNonNull(fileId, "fileId cannot be null");
        this.filename = Objects.requireNonNull(filename, "filename cannot be null");
        this.mimeType = mimeType;
        this.bytes = bytes;
        this.sourceUrl = sourceUrl;
        this.signedUrl = signedUrl;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.requestId = requestId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFilename() {
        return filename;
    }

    @Nullable
    public String getMimeType() {
        return mimeType;
    }

    @Nullable
    public Long getBytes() {
        return bytes;
    }

    @Nullable
    public String getSourceUrl() {
        return sourceUrl;
    }

    @Nullable
    public String getSignedUrl() {
        return signedUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public String getRequestId() {
        return requestId;
    }

    /**
     * Create a FileItem from a JSON object.
     */
    public static FileItem fromJson(JsonObject json) {
        String fileId = json.get("file_id").getAsString();
        String filename = json.get("filename").getAsString();
        String mimeType = json.has("mime_type") && !json.get("mime_type").isJsonNull()
                ? json.get("mime_type").getAsString() : null;
        Long bytes = json.has("bytes") && !json.get("bytes").isJsonNull()
                ? json.get("bytes").getAsLong() : null;
        String sourceUrl = json.has("source_url") && !json.get("source_url").isJsonNull()
                ? json.get("source_url").getAsString() : null;
        String signedUrl = json.has("signed_url") && !json.get("signed_url").isJsonNull()
                ? json.get("signed_url").getAsString() : null;
        String createdAt = json.get("created_at").getAsString();
        String requestId = json.has("request_id") && !json.get("request_id").isJsonNull()
                ? json.get("request_id").getAsString() : null;

        return new FileItem(fileId, filename, mimeType, bytes, sourceUrl, signedUrl, createdAt, requestId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileItem fileItem = (FileItem) o;
        return Objects.equals(fileId, fileItem.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId);
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "fileId='" + fileId + '\'' +
                ", filename='" + filename + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", bytes=" + bytes +
                ", signedUrl='" + (signedUrl != null ? signedUrl.substring(0, Math.min(50, signedUrl.length())) + "..." : null) + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
