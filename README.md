# Crebain API Client

Official SDKs for the Crebain API, available in **Python** and **Java**.

## Repository Contents

```
.
├── python/
│   ├── crebain_client-1.0.0-py3-none-any.whl   # Python SDK package
│   ├── test.py                                  # Full integration example
│   └── test_webhook.py                          # Webhook server example
├── java/
│   ├── crebain-client-1.0.0.jar                 # Java SDK (fat JAR with dependencies)
│   ├── pom.xml                                  # Maven build file
│   └── src/
│       ├── main/java/com/crebain/client/       # SDK source code
│       │   ├── CrebainClient.java
│       │   ├── model/                           # Response models
│       │   ├── request/                         # Request builders
│       │   ├── exception/                       # Exception classes
│       │   ├── webhook/WebhookVerifier.java
│       │   └── example/
│       │       ├── Test.java                    # Integration example
│       │       └── TestWebhook.java             # Webhook server example
│       └── test/java/                           # Unit tests
├── docs/
│   ├── API_CONTRACT.md                          # API contract specification
│   ├── CHANGELOG.md                             # Version history
│   ├── CLIENT_API_GUIDE.md                      # Comprehensive client guide
│   └── SLAS_LIMITS.md                           # Rate limits and SLAs
└── README.md
```

---

## Python SDK

### Installation

```bash
# Clone the repo
git clone https://github.com/crebaindata/api-install.git
cd api-install

# Install the SDK
pip install python/crebain_client-1.0.0-py3-none-any.whl
```

### Quickstart (Python)

```python
from crebain_client import CrebainClient

# Initialize the client
client = CrebainClient(
    api_key="ck_live_XXXXXXXXXXXXXXXX",
    base_url="https://<PROJECT_REF>.supabase.co/functions/v1/api",
    supabase_anon_key="<SUPABASE_ANON_KEY>"  # Optional
)

# List entities
page = client.list_entities(limit=10)
for entity in page.entities:
    print(f"{entity.id}: {entity.name}")

# Submit/create an entity
result = client.submit_entity(
    external_entity_id="customer-123",
    name="Acme Corp",
    metadata={"sector": "FinTech"},
    idempotency_key="unique-request-id"
)
print(f"Entity ID: {result.entity_id}")
print(f"New company: {result.new_company}")

# Download available files
for file in result.existing_files:
    print(f"{file.filename} -> {file.signed_url}")
```

### Run Python Examples

```bash
# Set environment variables
export CREBAIN_API_KEY="ck_live_..."
export CREBAIN_BASE_URL="https://<project>.supabase.co/functions/v1/api"
export SUPABASE_ANON_KEY="eyJhbG..."

# Run integration example
python python/test.py

# Run webhook server
python python/test_webhook.py

# Register a webhook (after starting ngrok)
python python/test_webhook.py --register https://xxx.ngrok.io/webhook
```

---

## Java SDK

### Installation

**Option 1: Use the pre-built JAR**

```bash
# The JAR includes all dependencies
cp java/crebain-client-1.0.0.jar /path/to/your/project/libs/
```

**Option 2: Build from source**

```bash
cd java
mvn package -DskipTests
# JAR will be at target/crebain-client-1.0.0.jar
```

**Option 3: Maven dependency (local install)**

```bash
cd java
mvn install -DskipTests
```

Then add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.crebain</groupId>
    <artifactId>crebain-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Quickstart (Java)

```java
import com.crebain.client.CrebainClient;
import com.crebain.client.model.*;
import com.crebain.client.request.*;

import java.time.Duration;

// Initialize the client
CrebainClient client = CrebainClient.builder()
    .apiKey("ck_live_XXXXXXXXXXXXXXXX")
    .baseUrl("https://<PROJECT_REF>.supabase.co/functions/v1/api")
    .supabaseAnonKey("<SUPABASE_ANON_KEY>")  // Optional
    .timeout(Duration.ofSeconds(30))
    .build();

// List entities
EntitiesPage page = client.listEntities(10, null);
for (Entity entity : page.getEntities()) {
    System.out.println(entity.getId() + ": " + entity.getName());
}

// Submit/create an entity
EntitySubmitResult result = client.submitEntity(
    SubmitEntityRequest.builder()
        .externalEntityId("customer-123")
        .name("Acme Corp")
        .metadata(Map.of("sector", "FinTech"))
        .idempotencyKey("unique-request-id")
        .build()
);
System.out.println("Entity ID: " + result.getEntityId());
System.out.println("New company: " + result.isNewCompany());

// Download available files
for (FileItem file : result.getExistingFiles()) {
    System.out.println(file.getFilename() + " -> " + file.getSignedUrl());
}

// Close the client when done
client.close();
```

### Run Java Examples

```bash
# Set environment variables
export CREBAIN_API_KEY="ck_live_..."
export CREBAIN_BASE_URL="https://<project>.supabase.co/functions/v1/api"
export SUPABASE_ANON_KEY="eyJhbG..."
export WEBHOOK_SECRET="whsec_your_secret_here"

# Run integration example
java -jar java/crebain-client-1.0.0.jar

# Run webhook server
java -cp java/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook

# Register a webhook (after starting ngrok)
java -cp java/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook --register https://xxx.ngrok.io/webhook

# List webhooks
java -cp java/crebain-client-1.0.0.jar com.crebain.client.example.TestWebhook --list
```

---

## API Methods

Both SDKs provide identical functionality:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `list_entities()` / `listEntities()` | GET `/v1/entities` | List entities with pagination |
| `iter_entities()` / `iterEntities()` | - | Auto-paginating iterator |
| `submit_entity()` / `submitEntity()` | POST `/v1/entity/submit` | Submit company for enrichment |
| `submit_person()` / `submitPerson()` | POST `/v1/person/submit` | Submit person for adverse news check |
| `get_request()` / `getRequest()` | GET `/v1/requests/{id}` | Get request status + files |
| `list_requests()` / `listRequests()` | GET `/v1/requests` | List async requests |
| `files_from_urls()` / `filesFromUrls()` | POST `/v1/files/from-urls` | Ingest files from URLs |
| `create_webhook()` / `createWebhook()` | POST `/v1/webhooks` | Create webhook |
| `list_webhooks()` / `listWebhooks()` | GET `/v1/webhooks` | List webhooks |
| `delete_webhook()` / `deleteWebhook()` | DELETE `/v1/webhooks/{id}` | Delete webhook |

---

## How It Works

1. **Submit Entity** - Call `submit_entity()` with a company name. If the entity doesn't exist, it will be created and enrichment will begin.
2. **Get Signed URLs** - The response includes `existing_files` with temporary signed URLs for any files ready for download.
3. **Download Files** - Use the signed URLs to download files (URLs are valid for ~15 minutes).
4. **Webhooks** (optional) - Register a webhook to receive notifications when async processing completes.

---

## Features

| Feature | Python | Java |
|---------|--------|------|
| Typed responses | Dataclasses | POJOs with getters |
| Auto-pagination | `iter_entities()` | `iterEntities()` returns `Iterator<Entity>` |
| Idempotency support | `idempotency_key` param | `idempotencyKey()` in request builders |
| Webhook verification | `verify_signature()` | `WebhookVerifier.verify()` |
| Error handling | Typed exceptions | Typed exceptions |
| Resource management | Context manager | `AutoCloseable` / `try-with-resources` |

---

## `submit_entity()` Options

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `name` | string | Yes* | Company name |
| `external_entity_id` | string | No | Your unique ID for deduplication |
| `company_description` | string | No | Description for enrichment context |
| `force` | bool | No | Re-run enrichment even if files exist |
| `fields` | list | No | Which enrichment outputs to generate |
| `idempotency_key` | string | No | Unique key for safe retries |

### Available `fields` Values

`Adverse_news_founder`, `Adverse_news_directors`, `Adverse_news_entities`, `Corporate_graph_funding_vehicles`, `People_control_report`, `Director_graph`

---

## `submit_person()` Options

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `name` | string | **Yes** | Person's full name |
| `company_name` | string | No | Company name for context |
| `entity_id` | string | No | Entity ID to link person to |
| `idempotency_key` | string | No | Unique key for safe retries |

---

## Error Handling

### Python

```python
from crebain_client import (
    ApiError, UnauthorizedError, RateLimitedError, ValidationError
)

try:
    result = client.submit_entity(name="Test")
except RateLimitedError as e:
    print(f"Rate limited! Request ID: {e.request_id}")
except ValidationError as e:
    print(f"Invalid request: {e.message}")
except ApiError as e:
    print(f"API error [{e.code}]: {e.message}")
```

### Java

```java
import com.crebain.client.exception.*;

try {
    EntitySubmitResult result = client.submitEntity(request);
} catch (RateLimitedException e) {
    System.out.println("Rate limited! Request ID: " + e.getRequestId());
} catch (ValidationException e) {
    System.out.println("Invalid request: " + e.getErrorMessage());
} catch (ApiException e) {
    System.out.println("API error [" + e.getCode() + "]: " + e.getErrorMessage());
}
```

### Exception Classes

| Exception | HTTP Status | Error Codes |
|-----------|-------------|-------------|
| `UnauthorizedError/Exception` | 401 | UNAUTHORIZED, API_KEY_REVOKED, API_KEY_EXPIRED |
| `ForbiddenError/Exception` | 403 | FORBIDDEN |
| `NotFoundError/Exception` | 404 | NOT_FOUND |
| `ConflictError/Exception` | 409 | CONFLICT |
| `ValidationError/Exception` | 422 | VALIDATION_ERROR |
| `RateLimitedError/Exception` | 429 | RATE_LIMITED |
| `InternalError/Exception` | 500 | INTERNAL |

---

## Webhook Verification

### Python

```python
from crebain_client import verify_signature

@app.route('/webhook', methods=['POST'])
def handle_webhook():
    if not verify_signature(
        WEBHOOK_SECRET,
        request.headers.get('X-Crebain-Timestamp'),
        request.get_data(),
        request.headers.get('X-Crebain-Signature')
    ):
        return 'Invalid signature', 401

    event = request.json
    # Process event...
    return 'OK', 200
```

### Java

```java
import com.crebain.client.webhook.WebhookVerifier;

// In your HTTP handler
boolean valid = WebhookVerifier.verify(
    WEBHOOK_SECRET,
    request.getHeader("X-Crebain-Timestamp"),
    request.getBodyBytes(),
    request.getHeader("X-Crebain-Signature")
);

if (!valid) {
    return ResponseEntity.status(401).body("Invalid signature");
}
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [API_CONTRACT.md](docs/API_CONTRACT.md) | API contract specification and endpoint details |
| [CLIENT_API_GUIDE.md](docs/CLIENT_API_GUIDE.md) | Comprehensive guide for using the client SDK |
| [CHANGELOG.md](docs/CHANGELOG.md) | Version history and release notes |
| [SLAS_LIMITS.md](docs/SLAS_LIMITS.md) | Rate limits and service level agreements |

---

## Requirements

### Python
- Python 3.10+
- `requests` library (included in wheel)

### Java
- Java 17+
- Dependencies bundled in JAR (OkHttp, Gson)

---

## License

MIT
