# Crebain API Contract

This document defines the stability guarantees, versioning policy, and behavioral contract for the Crebain API.

---

## Quick Reference

### Available Methods

| Method | Endpoint | Description |
|--------|----------|-------------|
| `list_entities()` | GET `/v1/entities` | List entities with pagination |
| `submit_entity()` | POST `/v1/entity/submit` | Submit company for enrichment |
| `submit_person()` | POST `/v1/person/submit` | Submit person for adverse news check |
| `files_from_urls()` | POST `/v1/files/from-urls` | Ingest files from URLs |
| `list_requests()` | GET `/v1/requests` | List async requests (with timestamps) |
| `get_request()` | GET `/v1/requests/{id}` | Get request status + files from that run |
| `list_webhooks()` | GET `/v1/webhooks` | List webhooks |
| `create_webhook()` | POST `/v1/webhooks` | Create webhook |
| `delete_webhook()` | DELETE `/v1/webhooks/{id}` | Delete webhook |

### Typical Flow

| Step | Action | Details |
|------|--------|---------|
| 1 | Submit | `submit_entity(name="X", force=True)` → `async_request_id` |
| 2 | Wait | Poll `get_request(id)` until `status="complete"` |
| 3 | Get files | `get_request(id).files` → only files from THIS run |
| 4 | History | `list_requests()` → see all past runs with timestamps |

### `submit_entity()` Options

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `name` | string | Yes* | Company name |
| `external_entity_id` | string | No | Your unique ID for deduplication |
| `company_description` | string | No | Description for enrichment context |
| `force` | bool | No | Re-run enrichment even if files exist |
| `fields` | list | No | Filter which file types to return |
| `idempotency_key` | string | No | Unique key for safe retries |

### `submit_entity()` Examples

| Use Case | Code |
|----------|------|
| Basic | `client.submit_entity(name="Acme Corp")` |
| With your ID | `client.submit_entity(external_entity_id="cust-123", name="Acme Corp")` |
| Force re-run | `client.submit_entity(name="Acme Corp", force=True)` |
| Filter fields | `client.submit_entity(name="Acme Corp", fields=["Director_graph"])` |
| With idempotency | `client.submit_entity(name="Acme Corp", idempotency_key="req-001")` |

### `submit_person()` Options

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `name` | string | **Yes** | Person's full name |
| `company_name` | string | No | Company name for context |
| `entity_id` | string | No | Entity ID to link person to |
| `idempotency_key` | string | No | Unique key for safe retries |

### `submit_person()` Examples

| Use Case | Code |
|----------|------|
| Basic | `client.submit_person(name="John Smith")` |
| With company | `client.submit_person(name="John Smith", company_name="Acme Corp")` |
| Link to entity | `client.submit_person(name="John Smith", entity_id="uuid-123")` |
| Full example | `client.submit_person(name="John Smith", company_name="Acme", entity_id="uuid", idempotency_key="p-001")` |

### `fields` Whitelist

`Adverse_news_founder`, `Adverse_news_directors`, `Adverse_news_entities`, `Corporate_graph_funding_vehicles`, `People_control_report`, `Director_graph`

---

## Table of Contents

- [Versioning](#versioning)
- [Stability Guarantees](#stability-guarantees)
- [Error Model](#error-model)
- [Response Envelope](#response-envelope)
- [Breaking vs Non-Breaking Changes](#breaking-vs-non-breaking-changes)
- [Deprecation Policy](#deprecation-policy)

---

## Versioning

### URL Path Versioning

The API version is specified in the URL path:

```
https://<project-ref>.supabase.co/functions/v1/api/v1/entities
                                                  ^^
                                              API version
```

### Current Version

**v1** - Stable, production-ready

### Version Lifecycle

| Status | Description |
|--------|-------------|
| **Stable** | Production-ready, breaking changes follow deprecation policy |
| **Deprecated** | Still functional, will be removed in future |
| **Sunset** | No longer available |

---

## Stability Guarantees

The following are guaranteed stable within a major version:

### Guaranteed Stable

- Endpoint paths (`/v1/entities`, `/v1/entity/submit`, etc.)
- HTTP methods for each endpoint
- Required request parameters and their types
- Response envelope structure (`data`, `error`, `request_id`)
- Error code values
- Authentication mechanism (`X-API-Key` header)
- Idempotency behavior for supported endpoints

### May Change (Non-Breaking)

- New optional request parameters
- New fields in response objects
- New error codes (clients should handle unknown codes gracefully)
- New endpoints
- New optional headers

---

## Error Model

### Error Response Structure

All errors follow this structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable description",
    "request_id": "uuid"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `code` | string | Machine-readable error code (stable) |
| `message` | string | Human-readable description (may change) |
| `request_id` | string | Unique request identifier for debugging |

### Error Codes

| HTTP Status | Code | Description |
|-------------|------|-------------|
| 400 | `INVALID_REQUEST` | Malformed JSON or invalid request format |
| 401 | `UNAUTHORIZED` | Missing or invalid API key |
| 401 | `API_KEY_REVOKED` | API key has been revoked |
| 401 | `API_KEY_EXPIRED` | API key has expired |
| 403 | `FORBIDDEN` | Valid key but insufficient permissions (scope) |
| 404 | `NOT_FOUND` | Requested resource does not exist |
| 409 | `CONFLICT` | Idempotency conflict or resource state conflict |
| 422 | `VALIDATION_ERROR` | Request body failed validation |
| 429 | `RATE_LIMITED` | Too many requests |
| 500 | `INTERNAL` | Server-side error |

### Client Handling Guidance

**Retryable Errors:**
- `429 RATE_LIMITED` - Retry with exponential backoff
- `500 INTERNAL` - Retry with exponential backoff (max 3 attempts)

**Non-Retryable Errors:**
- `400 INVALID_REQUEST` - Fix request format
- `401 UNAUTHORIZED` - Check API key
- `403 FORBIDDEN` - Check key scopes
- `409 CONFLICT` - Use different idempotency key or check resource state
- `422 VALIDATION_ERROR` - Fix request body

---

## Response Envelope

### Success Response

```json
{
  "data": {
    // Endpoint-specific payload
  },
  "request_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

### Error Response

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Description",
    "request_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
  }
}
```

### Response Headers

| Header | Description |
|--------|-------------|
| `X-Request-Id` | Unique request identifier (matches `request_id` in body) |
| `Content-Type` | Always `application/json` |

---

## Breaking vs Non-Breaking Changes

### Breaking Changes

The following require a new API version:

- Removing an endpoint
- Removing a required request parameter
- Removing a response field that clients depend on
- Changing the type of an existing field
- Changing the meaning of an existing field
- Changing authentication requirements
- Changing error codes for existing error conditions

### Non-Breaking Changes

The following can be made without version increment:

- Adding new endpoints
- Adding optional request parameters
- Adding new fields to responses
- Adding new error codes
- Adding new enum values
- Improving error messages
- Performance improvements
- Bug fixes that align behavior with documentation

---

## Deprecation Policy

When a breaking change is necessary:

1. **Announcement**: Deprecation announced in [CHANGELOG.md](./CHANGELOG.md)
2. **Dual Support**: Both old and new behavior supported during transition
3. **Migration Guide**: Documentation provided for migration
4. **Sunset**: Old behavior removed after transition period

Clients should:
- Subscribe to changelog updates
- Handle unknown fields gracefully (ignore, don't fail)
- Handle unknown error codes gracefully (treat as generic error)

---

## Endpoint Reference

### Public Endpoints (API Key Auth)

| Method | Path | Scopes | Idempotent |
|--------|------|--------|------------|
| GET | `/v1/entities` | read | N/A |
| POST | `/v1/entity/submit` | write | Yes |
| POST | `/v1/person/submit` | write | Yes |
| GET | `/v1/requests` | read | N/A |
| GET | `/v1/requests/{id}` | read | N/A |
| POST | `/v1/files/from-urls` | write | Yes |
| GET | `/v1/webhooks` | read | N/A |
| POST | `/v1/webhooks` | write | Yes |
| DELETE | `/v1/webhooks/{id}` | write | No |

### POST /v1/entity/submit

Submit an entity for enrichment (or check existing files).

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `external_entity_id` | string | No | External identifier (unique per org) |
| `name` | string | No | Entity/company name |
| `company_description` | string | No | Brief description of the company (for enrichment context) |
| `metadata` | object | No | Additional metadata |
| `force` | boolean | No | Force enrichment even if files exist |
| `adverse_news_only` | boolean | No | Only check for adverse news |
| `fields` | string[] | No | Specifies which enrichment outputs to generate (see whitelist below) |

**Allowed `fields` values (whitelist):**
- `Adverse_news_founder`
- `Adverse_news_directors`
- `Adverse_news_entities`
- `Corporate_graph_funding_vehicles`
- `People_control_report`
- `Director_graph`

When `fields` is provided:
1. The enrichment job will **only generate** the specified output types (not all outputs)
2. The `existing_files` array in the response will only include files matching the requested fields

Both HTML and PDF variants are included when available. If `fields` is omitted, all output types are generated (default behavior).

**Example Request 1 (no fields - existing behavior):**

```json
{
  "external_entity_id": "customer-123",
  "name": "Acme Corp",
  "company_description": "Global fintech company specializing in B2B payment solutions",
  "metadata": {"sector": "FinTech"}
}
```

**Example Request 2 (with fields filter):**

```json
{
  "external_entity_id": "customer-456",
  "name": "Beta Industries",
  "fields": ["People_control_report", "Director_graph"]
}
```

**Response:**

```json
{
  "data": {
    "entity_id": "uuid",
    "new_company": true,
    "existing_files": [],
    "request_submitted": true,
    "async_request_id": "uuid"
  },
  "request_id": "api-request-uuid"
}
```

### POST /v1/person/submit

Submit a person for adverse news check (founder-focused). Returns only `Adverse_news_founder` outputs.

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Person's full name |
| `company_name` | string | No | Company name for context |
| `entity_id` | string | No | Entity ID to associate the person with |

**Example Request:**

```json
{
  "name": "John Smith",
  "company_name": "Acme Corp",
  "entity_id": "entity-uuid-123"
}
```

**Response:**

```json
{
  "data": {
    "person_name": "John Smith",
    "company_name": "Acme Corp",
    "entity_id": "entity-uuid-123",
    "existing_files": [],
    "request_submitted": true,
    "async_request_id": "uuid"
  },
  "request_id": "api-request-uuid"
}
```

### GET /v1/requests

List async requests with pagination and optional filters.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `limit` | integer | No | Max 200, default 50 |
| `cursor` | string | No | Pagination cursor |
| `status` | string | No | Filter by status (submitted, processing, complete, failed) |
| `kind` | string | No | Filter by kind (entity_enrich, url_ingest) |

**Response:**

```json
{
  "data": {
    "requests": [
      {
        "id": "uuid",
        "kind": "entity_enrich",
        "status": "complete",
        "payload": {...},
        "result": {...},
        "created_at": "2024-01-01T00:00:00Z",
        "updated_at": "2024-01-01T00:01:00Z",
        "completed_at": "2024-01-01T00:01:00Z"
      }
    ],
    "next_cursor": "optional-cursor"
  },
  "request_id": "api-request-uuid"
}
```

### GET /v1/requests/{id}

Get details of a specific async request by ID.

**Response:**

```json
{
  "data": {
    "id": "uuid",
    "kind": "entity_enrich",
    "status": "complete",
    "payload": {...},
    "result": {...},
    "files": [
      {
        "file_id": "uuid",
        "filename": "People_control_report.pdf",
        "mime_type": "application/pdf",
        "bytes": 12345,
        "signed_url": "https://...",
        "created_at": "2024-01-01T00:00:00Z"
      }
    ],
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-01T00:01:00Z",
    "completed_at": "2024-01-01T00:01:00Z"
  },
  "request_id": "api-request-uuid"
}
```

### POST /v1/files/from-urls

Get files from URLs or trigger ingestion for missing ones.

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `url_list` | string[] | Yes | List of URLs (max 50) |
| `entity_id` | string | No | Entity to associate files with |
| `force` | boolean | No | Force re-ingestion of all URLs |

**Response:**

```json
{
  "data": {
    "files": [...],
    "missing": ["https://..."],
    "request_submitted": true,
    "async_request_id": "uuid"
  },
  "request_id": "api-request-uuid"
}
```

### Admin Endpoints (Admin Key Auth)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/admin/keys` | List API keys for an org |
| POST | `/v1/admin/keys` | Create API key |
| POST | `/v1/admin/keys/{id}/revoke` | Revoke API key |

Admin endpoints require the `X-Admin-Key` header instead of `X-API-Key`.

---

## OpenAPI Specification

The complete API specification is available in [openapi.yaml](../openapi.yaml).

The OpenAPI spec is the authoritative source for:
- Request/response schemas
- Parameter definitions
- Example payloads
