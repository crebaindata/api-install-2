# Changelog

All notable changes to the Crebain API will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.0.0] - 2026-02-04

### Changed

- **Breaking:** Renamed `POST /v1/entity/check` to `POST /v1/entity/submit`
- **Breaking:** Renamed response field `files_available` to `existing_files`
- **Breaking:** Renamed response field `request_id` (entity-specific) to `async_request_id`

### Added

- `POST /v1/person/submit` - Submit/create a person entity with enrichment
- `GET /v1/requests` - List async requests with cursor-based pagination
- `GET /v1/requests/{id}` - Get single request status and details
- `GET /v1/admin/keys/{id}` - Get API key details
- `PATCH /v1/admin/keys/{id}` - Update API key name/description

---

## [1.0.0] - 2024-01-15

### Added

- **Entity Management**
  - `GET /v1/entities` - List entities with cursor-based pagination
  - `POST /v1/entity/submit` - Submit or create entity, trigger enrichment

- **File Handling**
  - `POST /v1/files/from-urls` - Get existing files or trigger URL ingestion

- **Webhook Subscriptions**
  - `GET /v1/webhooks` - List webhook subscriptions
  - `POST /v1/webhooks` - Create webhook subscription
  - `DELETE /v1/webhooks/{id}` - Delete webhook subscription
  - Webhook signature verification (HMAC-SHA256)

- **Admin Endpoints**
  - `GET /v1/admin/keys` - List API keys
  - `POST /v1/admin/keys` - Create API key
  - `POST /v1/admin/keys/{id}/revoke` - Revoke API key

- **Core Features**
  - API key authentication with scopes (`read`, `write`)
  - Idempotency support for POST endpoints
  - Rate limiting (60 requests/minute per key)
  - Consistent error response format
  - Request ID tracking (`X-Request-Id` header)

---

## Changelog Template

Use this template for future releases:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security updates
```
