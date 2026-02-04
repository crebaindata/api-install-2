package com.crebain.client;

import com.crebain.client.exception.*;
import com.crebain.client.model.*;
import com.crebain.client.request.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CrebainClient.
 */
class CrebainClientTest {

    private MockWebServer mockServer;
    private CrebainClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        client = CrebainClient.builder()
                .apiKey("ck_test_123")
                .baseUrl(mockServer.url("/").toString())
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
        mockServer.shutdown();
    }

    // ==================== List Entities Tests ====================

    @Test
    void testListEntities() throws Exception {
        String responseJson = """
            {
                "data": {
                    "entities": [
                        {
                            "id": "uuid-1",
                            "external_entity_id": "ext-1",
                            "name": "Acme Corp",
                            "metadata": {"sector": "Tech"},
                            "created_at": "2024-01-01T00:00:00Z",
                            "updated_at": "2024-01-02T00:00:00Z"
                        }
                    ],
                    "next_cursor": "cursor-123"
                },
                "request_id": "req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        EntitiesPage page = client.listEntities(10, null);

        assertEquals(1, page.getEntities().size());
        assertEquals("uuid-1", page.getEntities().get(0).getId());
        assertEquals("Acme Corp", page.getEntities().get(0).getName());
        assertEquals("cursor-123", page.getNextCursor());
        assertTrue(page.hasNextPage());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertTrue(request.getPath().contains("/v1/entities"));
        assertEquals("ck_test_123", request.getHeader("X-API-Key"));
    }

    @Test
    void testListEntitiesNoCursor() throws Exception {
        String responseJson = """
            {
                "data": {
                    "entities": [],
                    "next_cursor": null
                },
                "request_id": "req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        EntitiesPage page = client.listEntities();

        assertEquals(0, page.getEntities().size());
        assertNull(page.getNextCursor());
        assertFalse(page.hasNextPage());
    }

    // ==================== Submit Entity Tests ====================

    @Test
    void testSubmitEntity() throws Exception {
        String responseJson = """
            {
                "data": {
                    "entity_id": "entity-uuid-123",
                    "new_company": true,
                    "existing_files": [],
                    "request_submitted": true,
                    "async_request_id": "async-req-456"
                },
                "request_id": "req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        SubmitEntityRequest request = SubmitEntityRequest.builder()
                .externalEntityId("customer-123")
                .name("Acme Corp")
                .companyDescription("A tech company")
                .force(true)
                .fields(List.of("People_control_report"))
                .idempotencyKey("idem-key-001")
                .build();

        EntitySubmitResult result = client.submitEntity(request);

        assertEquals("entity-uuid-123", result.getEntityId());
        assertTrue(result.isNewCompany());
        assertTrue(result.isRequestSubmitted());
        assertEquals("async-req-456", result.getAsyncRequestId());
        assertEquals(0, result.getExistingFiles().size());

        RecordedRequest httpRequest = mockServer.takeRequest();
        assertEquals("POST", httpRequest.getMethod());
        assertTrue(httpRequest.getPath().contains("/v1/entity/submit"));
        assertEquals("idem-key-001", httpRequest.getHeader("Idempotency-Key"));
        assertTrue(httpRequest.getBody().readUtf8().contains("customer-123"));
    }

    // ==================== Submit Person Tests ====================

    @Test
    void testSubmitPerson() throws Exception {
        String responseJson = """
            {
                "data": {
                    "person_name": "John Smith",
                    "company_name": "Acme Corp",
                    "entity_id": "entity-123",
                    "existing_files": [],
                    "request_submitted": true,
                    "async_request_id": "async-req-789"
                },
                "request_id": "req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        SubmitPersonRequest request = SubmitPersonRequest.builder("John Smith")
                .companyName("Acme Corp")
                .entityId("entity-123")
                .build();

        PersonSubmitResult result = client.submitPerson(request);

        assertEquals("John Smith", result.getPersonName());
        assertEquals("Acme Corp", result.getCompanyName());
        assertEquals("entity-123", result.getEntityId());
        assertTrue(result.isRequestSubmitted());
        assertEquals("async-req-789", result.getAsyncRequestId());
    }

    // ==================== Get Request Tests ====================

    @Test
    void testGetRequest() throws Exception {
        String responseJson = """
            {
                "data": {
                    "id": "req-123",
                    "kind": "entity_enrich",
                    "status": "complete",
                    "payload": {"name": "Acme"},
                    "result": {"file_ids": ["f1", "f2"]},
                    "files": [
                        {
                            "file_id": "file-uuid-1",
                            "filename": "report.pdf",
                            "mime_type": "application/pdf",
                            "bytes": 12345,
                            "signed_url": "https://storage.example.com/file1",
                            "created_at": "2024-01-01T00:00:00Z"
                        }
                    ],
                    "created_at": "2024-01-01T00:00:00Z",
                    "updated_at": "2024-01-01T00:01:00Z",
                    "completed_at": "2024-01-01T00:01:00Z"
                },
                "request_id": "api-req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        RequestInfo info = client.getRequest("req-123");

        assertEquals("req-123", info.getId());
        assertEquals("entity_enrich", info.getKind());
        assertEquals("complete", info.getStatus());
        assertTrue(info.isComplete());
        assertFalse(info.isFailed());
        assertEquals(1, info.getFiles().size());
        assertEquals("report.pdf", info.getFiles().get(0).getFilename());
    }

    // ==================== List Requests Tests ====================

    @Test
    void testListRequests() throws Exception {
        String responseJson = """
            {
                "data": {
                    "requests": [
                        {
                            "id": "req-1",
                            "kind": "entity_enrich",
                            "status": "complete",
                            "payload": {},
                            "result": null,
                            "created_at": "2024-01-01T00:00:00Z",
                            "updated_at": "2024-01-01T00:00:00Z",
                            "completed_at": null
                        }
                    ],
                    "next_cursor": null
                },
                "request_id": "api-req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        RequestsPage page = client.listRequests(10, null, "complete", "entity_enrich");

        assertEquals(1, page.getRequests().size());
        assertEquals("req-1", page.getRequests().get(0).getId());
        assertFalse(page.hasNextPage());

        RecordedRequest request = mockServer.takeRequest();
        assertTrue(request.getPath().contains("status=complete"));
        assertTrue(request.getPath().contains("kind=entity_enrich"));
    }

    // ==================== Files From URLs Tests ====================

    @Test
    void testFilesFromUrls() throws Exception {
        String responseJson = """
            {
                "data": {
                    "files": [
                        {
                            "file_id": "file-1",
                            "filename": "doc.pdf",
                            "source_url": "https://example.com/doc.pdf",
                            "signed_url": "https://storage.example.com/signed",
                            "created_at": "2024-01-01T00:00:00Z"
                        }
                    ],
                    "missing": ["https://example.com/missing.pdf"],
                    "request_submitted": true,
                    "async_request_id": "async-123"
                },
                "request_id": "api-req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        FilesFromUrlsRequest request = FilesFromUrlsRequest.builder(
                List.of("https://example.com/doc.pdf", "https://example.com/missing.pdf"))
                .entityId("entity-123")
                .build();

        FilesFromUrlsResult result = client.filesFromUrls(request);

        assertEquals(1, result.getFiles().size());
        assertEquals(1, result.getMissing().size());
        assertEquals("https://example.com/missing.pdf", result.getMissing().get(0));
        assertTrue(result.isRequestSubmitted());
        assertEquals("async-123", result.getAsyncRequestId());
    }

    // ==================== Webhook Tests ====================

    @Test
    void testCreateWebhook() throws Exception {
        String responseJson = """
            {
                "data": {
                    "id": "webhook-uuid-123",
                    "url": "https://myserver.com/webhook",
                    "enabled": true,
                    "created_at": "2024-01-01T00:00:00Z"
                },
                "request_id": "api-req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        WebhookSubscription webhook = client.createWebhook(
                "https://myserver.com/webhook",
                "whsec_secret_key_here",
                "idem-123"
        );

        assertEquals("webhook-uuid-123", webhook.getId());
        assertEquals("https://myserver.com/webhook", webhook.getUrl());
        assertTrue(webhook.isEnabled());

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("idem-123", request.getHeader("Idempotency-Key"));
    }

    @Test
    void testListWebhooks() throws Exception {
        String responseJson = """
            {
                "data": {
                    "webhooks": [
                        {
                            "id": "wh-1",
                            "url": "https://a.com/webhook",
                            "enabled": true,
                            "created_at": "2024-01-01T00:00:00Z"
                        },
                        {
                            "id": "wh-2",
                            "url": "https://b.com/webhook",
                            "enabled": false,
                            "created_at": "2024-01-02T00:00:00Z"
                        }
                    ]
                },
                "request_id": "api-req-abc"
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        List<WebhookSubscription> webhooks = client.listWebhooks();

        assertEquals(2, webhooks.size());
        assertEquals("wh-1", webhooks.get(0).getId());
        assertTrue(webhooks.get(0).isEnabled());
        assertFalse(webhooks.get(1).isEnabled());
    }

    @Test
    void testDeleteWebhook() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setBody("{\"data\": {}, \"request_id\": \"req-abc\"}")
                .addHeader("Content-Type", "application/json"));

        assertDoesNotThrow(() -> client.deleteWebhook("webhook-123"));

        RecordedRequest request = mockServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertTrue(request.getPath().contains("/v1/webhooks/webhook-123"));
    }

    // ==================== Error Handling Tests ====================

    @Test
    void testUnauthorizedError() {
        String errorJson = """
            {
                "error": {
                    "code": "UNAUTHORIZED",
                    "message": "Invalid API key",
                    "request_id": "req-err-123"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> client.listEntities()
        );

        assertEquals("UNAUTHORIZED", exception.getCode());
        assertEquals("Invalid API key", exception.getErrorMessage());
        assertEquals("req-err-123", exception.getRequestId());
        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void testForbiddenError() {
        String errorJson = """
            {
                "error": {
                    "code": "FORBIDDEN",
                    "message": "Insufficient permissions",
                    "request_id": "req-err-456"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> client.listEntities()
        );

        assertEquals("FORBIDDEN", exception.getCode());
    }

    @Test
    void testNotFoundError() {
        String errorJson = """
            {
                "error": {
                    "code": "NOT_FOUND",
                    "message": "Resource not found",
                    "request_id": "req-err-789"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> client.getRequest("non-existent")
        );

        assertEquals("NOT_FOUND", exception.getCode());
    }

    @Test
    void testConflictError() {
        String errorJson = """
            {
                "error": {
                    "code": "CONFLICT",
                    "message": "Idempotency key conflict",
                    "request_id": "req-err-conflict"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(409)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> client.submitEntity(SubmitEntityRequest.builder().name("Test").build())
        );

        assertEquals("CONFLICT", exception.getCode());
    }

    @Test
    void testValidationError() {
        String errorJson = """
            {
                "error": {
                    "code": "VALIDATION_ERROR",
                    "message": "url_list cannot exceed 50 URLs",
                    "request_id": "req-err-validation"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(422)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> client.filesFromUrls(FilesFromUrlsRequest.builder(List.of("https://example.com")).build())
        );

        assertEquals("VALIDATION_ERROR", exception.getCode());
    }

    @Test
    void testRateLimitedError() {
        String errorJson = """
            {
                "error": {
                    "code": "RATE_LIMITED",
                    "message": "Rate limit exceeded",
                    "request_id": "req-err-rate"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        RateLimitedException exception = assertThrows(
                RateLimitedException.class,
                () -> client.listEntities()
        );

        assertEquals("RATE_LIMITED", exception.getCode());
    }

    @Test
    void testInternalError() {
        String errorJson = """
            {
                "error": {
                    "code": "INTERNAL",
                    "message": "Internal server error",
                    "request_id": "req-err-internal"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        InternalException exception = assertThrows(
                InternalException.class,
                () -> client.listEntities()
        );

        assertEquals("INTERNAL", exception.getCode());
    }

    @Test
    void testErrorFallbackByStatusCode() {
        // Test that unknown error codes fall back to status code mapping
        String errorJson = """
            {
                "error": {
                    "code": "SOME_NEW_ERROR",
                    "message": "Some new error type",
                    "request_id": "req-unknown"
                }
            }
            """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        // Should still throw UnauthorizedException based on status code
        assertThrows(UnauthorizedException.class, () -> client.listEntities());
    }
}
