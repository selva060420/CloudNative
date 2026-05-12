# 09 — REST API Design

## 1. Definition

REST (Representational State Transfer) is an architectural style for designing networked applications. A RESTful API uses HTTP methods to operate on resources identified by URIs, returning representations (JSON/XML) with appropriate status codes.

REST is **stateless** — each request contains all information needed to process it. The server stores no client session state between requests.

---

## 2. Why This Is Needed

| Problem | REST Solution |
|---|---|
| Tight coupling between client/server | Uniform interface — clients only need to know resource URIs |
| Platform-specific protocols (CORBA, RMI) | HTTP — universally supported, firewall-friendly |
| Complex SOAP/WSDL contracts | Simple JSON + HTTP verbs — easy to understand and test |
| Stateful servers limit scaling | Stateless — any server instance can handle any request |
| No caching | HTTP caching built-in (ETags, Cache-Control) |
| Difficult to evolve APIs | Versioning strategies allow backward-compatible changes |

---

## 3. Core Concepts

### HTTP Methods (CRUD Mapping)

| Method | Operation | Idempotent | Safe | Example |
|--------|-----------|-----------|------|---------|
| GET | Read | ✅ | ✅ | `GET /users/123` |
| POST | Create | ❌ | ❌ | `POST /users` |
| PUT | Full update/replace | ✅ | ❌ | `PUT /users/123` |
| PATCH | Partial update | ❌* | ❌ | `PATCH /users/123` |
| DELETE | Remove | ✅ | ❌ | `DELETE /users/123` |

*PATCH can be made idempotent depending on implementation.

### Status Codes

| Range | Category | Common Codes |
|-------|----------|-------------|
| 2xx | Success | 200 OK, 201 Created, 204 No Content |
| 3xx | Redirection | 301 Moved, 304 Not Modified |
| 4xx | Client Error | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 429 Too Many Requests |
| 5xx | Server Error | 500 Internal, 502 Bad Gateway, 503 Service Unavailable |

### Resource Naming Conventions

```
✅ Good:
GET    /users              → list users
GET    /users/123          → get user 123
POST   /users              → create user
GET    /users/123/orders   → get orders for user 123

❌ Bad:
GET    /getUser?id=123     → verb in URL
POST   /createUser         → verb in URL
GET    /user               → singular for collection
```

---

## 4. Richardson Maturity Model

| Level | Description | Example |
|-------|-------------|---------|
| 0 — Swamp of POX | Single URI, single verb (POST everything) | `POST /api` with action in body |
| 1 — Resources | Multiple URIs, but only POST | `POST /users`, `POST /orders` |
| 2 — HTTP Verbs | Proper use of GET/POST/PUT/DELETE + status codes | `GET /users/123` → 200 |
| 3 — Hypermedia (HATEOAS) | Responses include links to related actions | `"_links": {"self": "/users/123", "orders": "/users/123/orders"}` |

Most production APIs are Level 2. Level 3 (HATEOAS) is the "true REST" but rarely fully implemented.

---

## 5. Idempotency

An operation is **idempotent** if calling it multiple times produces the same result as calling it once.

**Why it matters:** Network failures cause retries. Without idempotency, retrying a POST could create duplicate resources.

```
PUT /users/123 {name: "John"}   → Always results in user 123 being "John"
DELETE /users/123               → First call deletes, subsequent calls return 404 (same end state)
POST /users {name: "John"}     → Each call creates a NEW user (not idempotent)
```

**Making POST idempotent:**
```
POST /payments
Idempotency-Key: abc-123-def

→ Server checks: have I seen key "abc-123-def" before?
→ Yes: return cached response (don't process again)
→ No: process and store result keyed by "abc-123-def"
```

---

## 6. Pagination

### Offset-based (simple, common)
```
GET /users?page=2&size=20

Response:
{
  "content": [...],
  "page": 2,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```
⚠️ Problem: Inserting/deleting rows shifts pages — can skip or duplicate items.

### Cursor-based (scalable, consistent)
```
GET /users?cursor=eyJpZCI6MTAwfQ&limit=20

Response:
{
  "data": [...],
  "nextCursor": "eyJpZCI6MTIwfQ",
  "hasMore": true
}
```
✅ No skipping/duplicating. Efficient for large datasets (no OFFSET scan).

---

## 7. Versioning Strategies

| Strategy | Example | Pros | Cons |
|----------|---------|------|------|
| URI path | `/v1/users`, `/v2/users` | Simple, visible, cacheable | Pollutes URI space |
| Query param | `/users?version=2` | Easy to default | Easy to miss |
| Header | `Accept: application/vnd.api.v2+json` | Clean URIs | Hidden, harder to test |
| Content negotiation | `Accept: application/vnd.company.v2+json` | Most RESTful | Complex |

**Most common in practice:** URI path versioning (`/v1/`, `/v2/`).

---

## 8. Error Handling (RFC 7807 — Problem Details)

```json
{
  "type": "https://api.example.com/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Email field is required",
  "instance": "/users",
  "errors": [
    {"field": "email", "message": "must not be blank"},
    {"field": "age", "message": "must be >= 18"}
  ]
}
```

**Rules:**
- Always return consistent error structure
- Never expose stack traces to clients
- Include enough detail for the client to fix the request
- Use appropriate status codes (don't return 200 with error in body)

---

## 9. Content Negotiation

Client specifies desired format via `Accept` header:
```
GET /users/123
Accept: application/json        → returns JSON
Accept: application/xml         → returns XML
Accept: text/csv                → returns CSV
```

Server specifies what it sent via `Content-Type` header:
```
Content-Type: application/json; charset=utf-8
```

If server can't produce requested format → `406 Not Acceptable`.

---

## 10. HATEOAS (Hypermedia as the Engine of Application State)

Response includes links telling the client what actions are available:

```json
{
  "id": 123,
  "name": "John",
  "status": "ACTIVE",
  "_links": {
    "self": {"href": "/users/123"},
    "orders": {"href": "/users/123/orders"},
    "deactivate": {"href": "/users/123/deactivate", "method": "POST"}
  }
}
```

**Benefit:** Client doesn't hardcode URLs — it discovers available actions from the response. API can evolve without breaking clients.

---

## 11. Rate Limiting

Protects API from abuse and ensures fair usage.

**Response headers:**
```
X-RateLimit-Limit: 100          → max requests per window
X-RateLimit-Remaining: 23       → requests left
X-RateLimit-Reset: 1620000000   → when window resets (epoch)
```

When exceeded → `429 Too Many Requests`

**Common algorithms:**
- **Fixed window** — 100 req/minute, resets at minute boundary
- **Sliding window** — 100 req in any 60-second span
- **Token bucket** — tokens refill at steady rate, burst allowed up to bucket size
- **Leaky bucket** — requests processed at fixed rate, excess queued/dropped

---

## 12. REST vs gRPC

| Aspect | REST | gRPC |
|--------|------|------|
| Protocol | HTTP/1.1 (or 2) | HTTP/2 only |
| Format | JSON (text) | Protobuf (binary) |
| Contract | OpenAPI/Swagger (optional) | .proto file (required) |
| Streaming | Limited (SSE, WebSocket) | Bidirectional streaming built-in |
| Performance | Slower (text parsing) | 2-10x faster (binary, multiplexing) |
| Browser support | Native | Needs gRPC-Web proxy |
| Use case | Public APIs, CRUD | Internal service-to-service, real-time |
| Code generation | Optional | Built-in (polyglot) |

**When to use REST:** Public-facing APIs, simple CRUD, broad client compatibility.
**When to use gRPC:** Internal microservice communication, streaming, low-latency requirements.

---

## Interview Questions & Answers

**Q: What makes an API RESTful?**
A: Stateless, resource-based URIs, proper HTTP methods, standard status codes, uniform interface. Optionally HATEOAS for Level 3 maturity.

**Q: PUT vs PATCH?**
A: PUT replaces the entire resource (send all fields). PATCH updates only specified fields. PUT is idempotent by definition; PATCH may or may not be.

**Q: How do you handle versioning?**
A: URI path (`/v1/users`) is most common. Use it when breaking changes are needed. Keep old versions running during migration period. Deprecate with headers (`Sunset: date`).

**Q: How do you make POST idempotent?**
A: Client sends an `Idempotency-Key` header. Server stores the result keyed by that value. On retry with same key, return cached response without re-processing.

**Q: Pagination — offset vs cursor?**
A: Offset is simple but breaks with concurrent inserts/deletes and is slow for large offsets (DB scans). Cursor-based is consistent and performant but can't jump to arbitrary pages.

**Q: How do you handle partial failures in REST?**
A: Return `207 Multi-Status` for batch operations, or use async processing with `202 Accepted` + polling endpoint for status.

---

## Code Examples

| File | Description |
|------|-------------|
| `ProductController.java` | REST controller with CRUD, proper status codes, pagination |
| `GlobalExceptionHandler.java` | Centralized error handling with RFC 7807 format |
| `ApiVersioningDemo.java` | URI-path and header-based versioning examples |

---

## Practice Task

**Build a Product REST API with:**

1. CRUD endpoints following REST conventions
2. Pagination (offset-based with page metadata)
3. Global exception handler returning RFC 7807 error format
4. Input validation with meaningful error messages
5. URI versioning (`/api/v1/products`)
6. Rate limiting headers in responses

**Bonus:** Add HATEOAS links to responses and cursor-based pagination as an alternative endpoint.
