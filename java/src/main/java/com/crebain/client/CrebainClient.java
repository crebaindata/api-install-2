package com.crebain.client;

import com.crebain.client.exception.*;
import com.crebain.client.model.*;
import com.crebain.client.request.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Client for the Crebain API.
 * <p>
 * Use the {@link #builder()} method to create instances.
 *
 * <pre>{@code
 * CrebainClient client = CrebainClient.builder()
 *     .apiKey("ck_live_...")
 *     .baseUrl("https://xxx.supabase.co/functions/v1/api")
 *     .supabaseAnonKey("eyJhbG...")  // optional
 *     .timeout(Duration.ofSeconds(30))
 *     .build();
 *
 * // List entities
 * EntitiesPage page = client.listEntities(10, null);
 * for (Entity entity : page.getEntities()) {
 *     System.out.println(entity.getName());
 * }
 *
 * // Don't forget to close
 * client.close();
 * }</pre>
 */
public class CrebainClient implements Closeable {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    private final String apiKey;
    private final String baseUrl;
    @Nullable
    private final String supabaseAnonKey;
    private final OkHttpClient httpClient;

    private CrebainClient(Builder builder) {
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey is required");
        this.baseUrl = Objects.requireNonNull(builder.baseUrl, "baseUrl is required").replaceAll("/+$", "");
        this.supabaseAnonKey = builder.supabaseAnonKey;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(builder.timeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(builder.timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(builder.timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * Create a new client builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Entity Methods ====================

    /**
     * List entities with pagination.
     *
     * @param limit  Number of entities to return (max 200, default 50)
     * @param cursor Pagination cursor from previous response
     * @return EntitiesPage with entities and optional next_cursor
     */
    public EntitiesPage listEntities(int limit, @Nullable String cursor) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v1/entities").newBuilder()
                .addQueryParameter("limit", String.valueOf(limit));
        if (cursor != null) {
            urlBuilder.addQueryParameter("cursor", cursor);
        }

        JsonObject data = executeRequest("GET", urlBuilder.build().toString(), null, null);
        String requestId = data.has("_request_id") ? data.get("_request_id").getAsString() : "";
        return EntitiesPage.fromJson(data, requestId);
    }

    /**
     * List entities with default limit (50).
     */
    public EntitiesPage listEntities() {
        return listEntities(50, null);
    }

    /**
     * Iterate over all entities with automatic pagination.
     *
     * @param limit Number of entities per page (max 200)
     * @return Iterator over all entities
     */
    public Iterator<Entity> iterEntities(int limit) {
        return new Iterator<>() {
            private String cursor = null;
            private Iterator<Entity> currentPage = Collections.emptyIterator();
            private boolean hasMore = true;

            @Override
            public boolean hasNext() {
                if (currentPage.hasNext()) {
                    return true;
                }
                if (!hasMore) {
                    return false;
                }
                // Fetch next page
                EntitiesPage page = listEntities(limit, cursor);
                currentPage = page.getEntities().iterator();
                cursor = page.getNextCursor();
                hasMore = cursor != null;
                return currentPage.hasNext();
            }

            @Override
            public Entity next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return currentPage.next();
            }
        };
    }

    /**
     * Iterate over all entities with default limit (200).
     */
    public Iterator<Entity> iterEntities() {
        return iterEntities(200);
    }

    /**
     * Submit an entity for enrichment.
     *
     * @param request The submit entity request
     * @return EntitySubmitResult with entity_id and status
     */
    public EntitySubmitResult submitEntity(SubmitEntityRequest request) {
        JsonObject body = new JsonObject();
        if (request.getExternalEntityId() != null) {
            body.addProperty("external_entity_id", request.getExternalEntityId());
        }
        if (request.getName() != null) {
            body.addProperty("name", request.getName());
        }
        if (request.getCompanyDescription() != null) {
            body.addProperty("company_description", request.getCompanyDescription());
        }
        if (request.getMetadata() != null) {
            body.add("metadata", gson.toJsonTree(request.getMetadata()));
        }
        if (request.isForce()) {
            body.addProperty("force", true);
        }
        if (request.isAdverseNewsOnly()) {
            body.addProperty("adverse_news_only", true);
        }
        if (request.getFields() != null) {
            JsonArray fieldsArray = new JsonArray();
            request.getFields().forEach(fieldsArray::add);
            body.add("fields", fieldsArray);
        }

        JsonObject data = executeRequest("POST", baseUrl + "/v1/entity/submit", body, request.getIdempotencyKey());
        String requestId = data.has("_request_id") ? data.get("_request_id").getAsString() : "";
        return EntitySubmitResult.fromJson(data, requestId);
    }

    // ==================== Person Methods ====================

    /**
     * Submit a person for adverse news check (founder-focused).
     *
     * @param request The submit person request
     * @return PersonSubmitResult with request status and files
     */
    public PersonSubmitResult submitPerson(SubmitPersonRequest request) {
        JsonObject body = new JsonObject();
        body.addProperty("name", request.getName());
        if (request.getCompanyName() != null) {
            body.addProperty("company_name", request.getCompanyName());
        }
        if (request.getEntityId() != null) {
            body.addProperty("entity_id", request.getEntityId());
        }

        JsonObject data = executeRequest("POST", baseUrl + "/v1/person/submit", body, request.getIdempotencyKey());
        String requestId = data.has("_request_id") ? data.get("_request_id").getAsString() : "";
        return PersonSubmitResult.fromJson(data, requestId);
    }

    // ==================== Request Methods ====================

    /**
     * Get details of an async request by ID.
     *
     * @param requestId The async request ID
     * @return RequestInfo with status, payload, result, and files
     */
    public RequestInfo getRequest(String requestId) {
        JsonObject data = executeRequest("GET", baseUrl + "/v1/requests/" + requestId, null, null);
        String apiRequestId = data.has("_request_id") ? data.get("_request_id").getAsString() : "";
        return RequestInfo.fromJson(data, apiRequestId);
    }

    /**
     * List async requests with pagination and optional filters.
     *
     * @param limit  Number of requests to return (max 200, default 50)
     * @param cursor Pagination cursor from previous response
     * @param status Filter by status (submitted, processing, complete, failed)
     * @param kind   Filter by kind (entity_enrich, url_ingest)
     * @return RequestsPage with requests and optional next_cursor
     */
    public RequestsPage listRequests(int limit, @Nullable String cursor, @Nullable String status, @Nullable String kind) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/v1/requests").newBuilder()
                .addQueryParameter("limit", String.valueOf(limit));
        if (cursor != null) {
            urlBuilder.addQueryParameter("cursor", cursor);
        }
        if (status != null) {
            urlBuilder.addQueryParameter("status", status);
        }
        if (kind != null) {
            urlBuilder.addQueryParameter("kind", kind);
        }

        JsonObject data = executeRequest("GET", urlBuilder.build().toString(), null, null);
        String requestId = data.has("_request_id") ? data.get("_request_id").getAsString() : "";
        return RequestsPage.fromJson(data, requestId);
    }

    /**
     * List async requests with default parameters.
     */
    public RequestsPage listRequests() {
        return listRequests(50, null, null, null);
    }

    // ==================== Files Methods ====================

    /**
     * Get files from URLs or trigger ingestion for missing ones.
     *
     * @param request The files from URLs request
     * @return FilesFromUrlsResult with available files, missing URLs, and status
     */
    public FilesFromUrlsResult filesFromUrls(FilesFromUrlsRequest request) {
        JsonObject body = new JsonObject();
        JsonArray urlArray = new JsonArray();
        request.getUrlList().forEach(urlArray::add);
        body.add("url_list", urlArray);

        if (request.getEntityId() != null) {
            body.addProperty("entity_id", request.getEntityId());
        }
        if (request.isForce()) {
            body.addProperty("force", true);
        }

        JsonObject data = executeRequest("POST", baseUrl + "/v1/files/from-urls", body, request.getIdempotencyKey());
        String requestId = data.has("_request_id") ? data.get("_request_id").getAsString() : "";
        return FilesFromUrlsResult.fromJson(data, requestId);
    }

    // ==================== Webhook Methods ====================

    /**
     * Create a webhook subscription.
     *
     * @param url            Webhook endpoint URL (must be HTTPS)
     * @param secret         Secret for HMAC signature verification (min 16 chars)
     * @param idempotencyKey Optional idempotency key
     * @return WebhookSubscription with the created webhook details
     */
    public WebhookSubscription createWebhook(String url, String secret, @Nullable String idempotencyKey) {
        JsonObject body = new JsonObject();
        body.addProperty("url", url);
        body.addProperty("secret", secret);

        JsonObject data = executeRequest("POST", baseUrl + "/v1/webhooks", body, idempotencyKey);
        return WebhookSubscription.fromJson(data);
    }

    /**
     * Create a webhook subscription without idempotency key.
     */
    public WebhookSubscription createWebhook(String url, String secret) {
        return createWebhook(url, secret, null);
    }

    /**
     * List all webhook subscriptions.
     *
     * @return List of WebhookSubscription objects
     */
    public List<WebhookSubscription> listWebhooks() {
        JsonObject data = executeRequest("GET", baseUrl + "/v1/webhooks", null, null);
        List<WebhookSubscription> webhooks = new ArrayList<>();
        if (data.has("webhooks") && data.get("webhooks").isJsonArray()) {
            JsonArray arr = data.getAsJsonArray("webhooks");
            for (int i = 0; i < arr.size(); i++) {
                webhooks.add(WebhookSubscription.fromJson(arr.get(i).getAsJsonObject()));
            }
        }
        return webhooks;
    }

    /**
     * Delete a webhook subscription.
     *
     * @param webhookId ID of the webhook to delete
     */
    public void deleteWebhook(String webhookId) {
        executeRequest("DELETE", baseUrl + "/v1/webhooks/" + webhookId, null, null);
    }

    // ==================== Internal Methods ====================

    private JsonObject executeRequest(String method, String url, @Nullable JsonObject body, @Nullable String idempotencyKey) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("X-API-Key", apiKey)
                .addHeader("Content-Type", "application/json");

        if (supabaseAnonKey != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + supabaseAnonKey);
        }

        if (idempotencyKey != null) {
            requestBuilder.addHeader("Idempotency-Key", idempotencyKey);
        }

        switch (method) {
            case "GET":
                requestBuilder.get();
                break;
            case "POST":
                requestBuilder.post(RequestBody.create(body != null ? body.toString() : "{}", JSON));
                break;
            case "DELETE":
                requestBuilder.delete(body != null ? RequestBody.create(body.toString(), JSON) : null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                raiseForError(response.code(), responseBody);
            }

            // Parse response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            // Extract data and request_id from success envelope
            String requestId = jsonResponse.has("request_id") ? jsonResponse.get("request_id").getAsString() : "";
            JsonObject data;
            if (jsonResponse.has("data")) {
                data = jsonResponse.getAsJsonObject("data");
            } else {
                data = jsonResponse;
            }
            // Store request_id in data for later extraction
            data.addProperty("_request_id", requestId);
            return data;

        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    private void raiseForError(int statusCode, String responseBody) {
        String code = "UNKNOWN";
        String message = "Unknown error";
        String requestId = null;

        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if (json.has("error") && json.get("error").isJsonObject()) {
                JsonObject error = json.getAsJsonObject("error");
                code = error.has("code") ? error.get("code").getAsString() : "UNKNOWN";
                message = error.has("message") ? error.get("message").getAsString() : "Unknown error";
                requestId = error.has("request_id") ? error.get("request_id").getAsString() : null;
            }
        } catch (Exception ignored) {
            message = responseBody.isEmpty() ? "Unknown error" : responseBody;
        }

        ApiException exception = switch (code) {
            case "UNAUTHORIZED", "API_KEY_REVOKED", "API_KEY_EXPIRED" ->
                    new UnauthorizedException(code, message, requestId, statusCode, responseBody);
            case "FORBIDDEN" ->
                    new ForbiddenException(code, message, requestId, statusCode, responseBody);
            case "NOT_FOUND" ->
                    new NotFoundException(code, message, requestId, statusCode, responseBody);
            case "CONFLICT" ->
                    new ConflictException(code, message, requestId, statusCode, responseBody);
            case "VALIDATION_ERROR" ->
                    new ValidationException(code, message, requestId, statusCode, responseBody);
            case "RATE_LIMITED" ->
                    new RateLimitedException(code, message, requestId, statusCode, responseBody);
            case "INTERNAL" ->
                    new InternalException(code, message, requestId, statusCode, responseBody);
            default -> switch (statusCode) {
                case 401 -> new UnauthorizedException(code, message, requestId, statusCode, responseBody);
                case 403 -> new ForbiddenException(code, message, requestId, statusCode, responseBody);
                case 404 -> new NotFoundException(code, message, requestId, statusCode, responseBody);
                case 409 -> new ConflictException(code, message, requestId, statusCode, responseBody);
                case 422 -> new ValidationException(code, message, requestId, statusCode, responseBody);
                case 429 -> new RateLimitedException(code, message, requestId, statusCode, responseBody);
                case 500 -> new InternalException(code, message, requestId, statusCode, responseBody);
                default -> new ApiException(code, message, requestId, statusCode, responseBody);
            };
        };

        throw exception;
    }

    /**
     * Close the underlying HTTP client.
     */
    @Override
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    /**
     * Get the base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    // ==================== Builder ====================

    public static class Builder {
        private String apiKey;
        private String baseUrl;
        private String supabaseAnonKey;
        private Duration timeout = Duration.ofSeconds(30);

        /**
         * Set the API key (format: ck_live_...).
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Set the base URL of the API.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Set the Supabase anonymous key for gateway authentication (optional).
         */
        public Builder supabaseAnonKey(String supabaseAnonKey) {
            this.supabaseAnonKey = supabaseAnonKey;
            return this;
        }

        /**
         * Set the request timeout (default: 30 seconds).
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Build the client.
         */
        public CrebainClient build() {
            return new CrebainClient(this);
        }
    }
}
