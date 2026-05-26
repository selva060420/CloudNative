# 15 — Spring Security

> Interview-ready guide for a 3+ year Java backend engineer targeting mid-senior roles.

---

## 1. Definition

**Spring Security** is a framework providing authentication, authorization, and protection against common attacks for Spring-based applications.

| Concept | One-liner |
|---------|-----------|
| **Authentication** | Verifying WHO you are (credentials → identity) |
| **Authorization** | Verifying WHAT you can do (identity → permissions) |
| **SecurityFilterChain** | Ordered chain of servlet filters that intercept every HTTP request |
| **JWT** | JSON Web Token — stateless auth token containing claims, signed by server |
| **OAuth2** | Authorization framework — delegate access without sharing credentials |
| **CORS** | Cross-Origin Resource Sharing — controls which domains can call your API |
| **CSRF** | Cross-Site Request Forgery — attack where malicious site makes requests on user's behalf |
| **Principal** | Currently authenticated user stored in SecurityContext |
| **GrantedAuthority** | Permission/role assigned to a user (e.g., ROLE_ADMIN) |

---

## 2. Why This Is Needed

| Problem | Spring Security Solution |
|---------|--------------------------|
| Unauthorized API access | Authentication filters validate JWT/session before reaching controller |
| Role-based access control | `@PreAuthorize("hasRole('ADMIN')")` on endpoints |
| Password storage | BCrypt hashing (never store plaintext) |
| CSRF attacks on web apps | CSRF token validation on state-changing requests |
| Cross-origin API calls blocked | CORS configuration allows specific origins |
| Session hijacking | Stateless JWT — no server-side session to steal |
| Brute-force login attempts | Rate limiting + account lockout |
| Microservice-to-microservice auth | OAuth2 client credentials flow |

---

## 3. How It Works Internally

### Security Filter Chain

```
HTTP Request
    ↓
┌─────────────────────────────────────────────┐
│           SecurityFilterChain                │
│                                             │
│  1. CorsFilter                              │
│  2. CsrfFilter                              │
│  3. UsernamePasswordAuthenticationFilter     │
│  4. BearerTokenAuthenticationFilter (JWT)    │
│  5. ExceptionTranslationFilter              │
│  6. AuthorizationFilter                     │
└─────────────────────────────────────────────┘
    ↓
Controller (only if all filters pass)
```

### JWT Authentication Flow

```
1. Client → POST /auth/login {username, password}
2. Server → validates credentials against DB
3. Server → generates JWT (header.payload.signature)
4. Server → returns JWT to client

5. Client → GET /api/orders (Authorization: Bearer <JWT>)
6. JwtFilter → extracts token from header
7. JwtFilter → validates signature + expiry
8. JwtFilter → sets Authentication in SecurityContext
9. AuthorizationFilter → checks roles/permissions
10. Controller → processes request
```

### JWT Structure

```
Header.Payload.Signature

Header:  {"alg": "HS256", "typ": "JWT"}
Payload: {"sub": "user123", "roles": ["ADMIN"], "exp": 1700000000}
Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
```

### OAuth2 Flows

| Flow | Use Case | How It Works |
|------|----------|--------------|
| **Authorization Code** | Web apps (most secure) | Redirect → auth server → code → exchange for token |
| **Client Credentials** | Service-to-service | Client ID + secret → token (no user involved) |
| **PKCE** | Mobile/SPA (public clients) | Code challenge/verifier prevents interception |
| **Refresh Token** | Long-lived sessions | Short-lived access token + long-lived refresh token |

---

## 4. Real-World Example

### 5G NEF API Security (Ericsson)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // stateless API — no CSRF needed
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }
}
```

---

## 5. Common Interview Questions

### Q1: Authentication vs Authorization?

| | Authentication | Authorization |
|---|----------------|---------------|
| Question | "Who are you?" | "What can you do?" |
| When | First (login) | After authentication |
| Mechanism | Username/password, JWT, OAuth2 | Roles, permissions, policies |
| HTTP code on failure | 401 Unauthorized | 403 Forbidden |
| Spring class | `AuthenticationManager` | `AccessDecisionManager` |

### Q2: How does JWT work? What are its pros/cons?

**Pros:**
- Stateless — no server-side session storage, scales horizontally
- Self-contained — carries user info + roles in payload
- Cross-service — any service with the secret/public key can validate

**Cons:**
- Can't revoke immediately (until expiry) — use short TTL + refresh tokens
- Payload is base64 (not encrypted) — don't store sensitive data
- Token size grows with claims — larger than session cookie

### Q3: How to handle JWT token revocation?

**Approaches:**
1. **Short-lived tokens** (15 min) + refresh token rotation
2. **Token blacklist** in Redis — check on every request (adds state)
3. **Token versioning** — store version in DB, increment on logout
4. **Refresh token revocation** — revoke refresh token, access token expires naturally

### Q4: Explain CORS. Why is it needed?

**Answer:** Browsers enforce Same-Origin Policy — JavaScript on `app.com` can't call `api.com` by default. CORS headers tell the browser which cross-origin requests are allowed.

```java
@Bean
public CorsConfigurationSource corsConfig() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.ericsson.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

### Q5: When to disable CSRF?

**Answer:** Disable CSRF for **stateless REST APIs** that use JWT/OAuth2 (no cookies = no CSRF risk). Keep CSRF enabled for **server-rendered web apps** that use session cookies.

CSRF attacks exploit the browser automatically sending cookies. If your API uses `Authorization: Bearer` header (not cookies), CSRF is not a threat.

### Q6: How does `@PreAuthorize` work?

```java
@PreAuthorize("hasRole('ADMIN')")           // role check
@PreAuthorize("hasAuthority('user:write')") // fine-grained permission
@PreAuthorize("#userId == authentication.principal.id") // object-level
public void deleteUser(Long userId) { ... }
```

Uses Spring AOP — a proxy intercepts the method call, checks the SecurityContext, and throws `AccessDeniedException` if unauthorized.

---

## 6. Tricky Edge Cases & Pitfalls

| Pitfall | What Happens | Fix |
|---------|-------------|-----|
| Storing JWT in localStorage | XSS attack can steal token | Use httpOnly cookie or in-memory storage |
| Long-lived JWT (24h+) | Can't revoke compromised token | Short TTL (15min) + refresh token |
| CORS `allowedOrigins("*")` with credentials | Browser rejects (spec violation) | Specify exact origins when using credentials |
| `@PreAuthorize` on private methods | AOP proxy can't intercept | Use on public methods only |
| BCrypt with low rounds | Fast brute-force | Use strength 12+ (`BCryptPasswordEncoder(12)`) |
| Not validating JWT `exp` claim | Expired tokens accepted | Always check expiry in filter |
| Filter order wrong | Auth filter runs after authorization | Ensure JWT filter is before `AuthorizationFilter` |
| Forgetting to clear SecurityContext | Previous user's context leaks to next request | `SecurityContextHolder.clearContext()` in filter finally block |

---

## 7. Comparison with Related Concepts

### Authentication Methods

| Method | Stateful? | Best For | Scalability |
|--------|-----------|----------|-------------|
| Session + Cookie | Yes (server stores session) | Monoliths, web apps | Needs sticky sessions or Redis |
| JWT | No (token is self-contained) | Microservices, APIs | Excellent (any instance validates) |
| OAuth2 + OIDC | Depends on implementation | Third-party login, SSO | Excellent |
| API Key | No | Service-to-service, simple APIs | Good (but no user context) |
| mTLS | No | Service mesh (Istio) | Excellent |

### Spring Security vs Manual Security

| Aspect | Spring Security | Manual Implementation |
|--------|----------------|---------------------|
| Filter chain | Built-in, configurable | Must build from scratch |
| Password hashing | BCrypt/Argon2 built-in | Must implement correctly |
| CSRF protection | Automatic for forms | Easy to forget |
| OAuth2/OIDC | Full client + resource server support | Weeks of work |
| Method security | `@PreAuthorize` annotation | Manual checks in every method |
| Security headers | Auto-adds X-Frame-Options, etc. | Must remember each header |

---

## 8. Performance Impact

| Aspect | Impact | Mitigation |
|--------|--------|-----------|
| JWT validation per request | ~1-5ms (signature verification) | Cache parsed token in request scope |
| BCrypt password hashing | ~100-300ms (by design — slow = secure) | Only on login, not every request |
| Database role lookup per request | ~5-10ms | Cache roles in JWT claims or Redis |
| CORS preflight (OPTIONS) | Extra round-trip for cross-origin | Browser caches preflight (`maxAge=3600`) |
| Filter chain overhead | ~1-2ms for full chain | Negligible for most APIs |
| RSA vs HMAC JWT signing | RSA ~10x slower | Use RSA for distributed (public key verification), HMAC for single service |

---

## 9. Trade-offs

| Decision | Option A | Option B | Recommendation |
|----------|----------|----------|----------------|
| Token storage | JWT (stateless) | Session (stateful) | JWT for microservices, session for monoliths |
| JWT signing | HMAC (symmetric) | RSA (asymmetric) | RSA when multiple services validate tokens |
| Token lifetime | Short (15min) | Long (24h) | Short + refresh token for security |
| Role storage | In JWT claims | Database lookup per request | JWT claims (avoid DB call per request) |
| CSRF | Enabled | Disabled | Disable for stateless APIs, enable for cookie-based |
| Password hash | BCrypt | Argon2id | Argon2id is newer/stronger, BCrypt is battle-tested |

---

## 10. 30–60 Second Interview Answers

### "Explain Spring Security architecture"

> "Spring Security is a filter chain that intercepts every HTTP request. Key filters include: CORS filter, CSRF filter, authentication filter (validates credentials or JWT), and authorization filter (checks roles). When a request arrives, the JWT filter extracts and validates the token, sets the Authentication object in SecurityContext, then the authorization filter checks if the user has the required role. If any filter fails, it short-circuits with 401 or 403."

### "How do you secure a REST API?"

> "I use stateless JWT authentication: disable sessions and CSRF (not needed without cookies), add a JWT filter that validates the Bearer token on every request, configure endpoint-level authorization with `authorizeHttpRequests`, and use `@PreAuthorize` for method-level security. For production: short-lived tokens (15 min), refresh token rotation, BCrypt for passwords, CORS restricted to known origins, and rate limiting to prevent brute force."

### "OAuth2 in 30 seconds"

> "OAuth2 is an authorization framework that lets users grant third-party apps limited access without sharing passwords. The Authorization Code flow: user is redirected to the auth server, logs in, gets a code, which the app exchanges for an access token. For service-to-service, Client Credentials flow: the service authenticates directly with client ID and secret to get a token. Spring Security has built-in support for both as a client and as a resource server."

---

## 11. Real Production Scenario

### Scenario: JWT Token Leak in Kubernetes Logs (Ericsson)

**Context:** NEF API secured with JWT. Tokens logged in request headers by default Spring Boot access logging.

**Symptom:** Security audit found JWT tokens in plaintext in ELK logs. Anyone with log access could impersonate users.

**Root Cause:** Default access log pattern included all headers: `%h %l %u %t "%r" %s %b` — the `%r` (request line) didn't include headers, but a custom filter was logging `request.getHeader("Authorization")` for debugging.

**Fix:**
```java
// Before (INSECURE)
log.info("Request: {} {}, Auth: {}", method, path, request.getHeader("Authorization"));

// After (SECURE)
log.info("Request: {} {}, Auth: Bearer ***masked***", method, path);

// Also: mask in MDC filter
String token = request.getHeader("Authorization");
if (token != null) {
    MDC.put("auth", "Bearer " + token.substring(token.length() - 4)); // last 4 chars only
}
```

Plus: Added log scrubbing rules in ELK pipeline to redact any `eyJ` (JWT prefix) patterns.

**Lesson:** Never log tokens, passwords, or PII. Use structured logging with explicit field selection.

---

## 12. If This Fails, How to Debug

| Symptom | Likely Cause | Debug |
|---------|-------------|-------|
| 401 on every request | JWT filter not parsing token, wrong header format | Enable `logging.level.org.springframework.security=DEBUG` |
| 403 after successful auth | Missing role, wrong role prefix (ROLE_ vs authority) | Check `SecurityContext.getAuthentication().getAuthorities()` |
| CORS error in browser | Missing CORS config or wrong allowed origins | Check browser Network tab → preflight OPTIONS response headers |
| Login works but subsequent requests fail | Token not sent in header, or session not created | Verify `Authorization: Bearer <token>` in request |
| `@PreAuthorize` not working | `@EnableMethodSecurity` missing, or called on private method | Add annotation to config class, use public methods |
| Token expired but no clear error | Generic 401 without details | Add custom `AuthenticationEntryPoint` with descriptive error |
| Filter chain order wrong | Custom filter runs at wrong position | Use `addFilterBefore/After` with explicit position |

### Debug Configuration

```properties
# Enable security debug logging
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.web.FilterChainProxy=DEBUG

# See which filters are active
spring.security.debug=true  # NEVER in production (logs sensitive data)
```

---

## Follow-Up Interview Questions

**Q1:** "Design the authentication system for a microservices architecture with 10 services. How do you handle service-to-service auth and user auth?"

**Answer:**

```
                    ┌──────────────┐
                    │  Auth Server  │ (Keycloak / custom)
                    │  Issues JWTs  │
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
   API Gateway        Service A          Service B
   (validates JWT)    (validates JWT)    (validates JWT)
```

**User auth:** API Gateway validates JWT on every request, forwards user claims in headers to downstream services. Each service trusts the gateway (or validates JWT independently with the public key).

**Service-to-service:** OAuth2 Client Credentials flow — each service has its own client ID/secret, requests a token from the auth server, includes it when calling other services. Alternatively, in Kubernetes with Istio: mTLS handles service identity automatically.

**Key decisions:**
- Centralized auth server (Keycloak) — single source of truth for users/roles
- RSA-signed JWTs — any service validates with public key (no shared secret)
- Short-lived tokens (15 min) — limits blast radius of token leak
- Service accounts with minimal permissions — principle of least privilege

---

**Q2:** "A penetration test found that your API is vulnerable to JWT algorithm confusion attack. What is it and how do you fix it?"

**Answer:**

**The attack:** Attacker changes JWT header from `{"alg": "RS256"}` to `{"alg": "HS256"}` and signs with the RSA **public key** (which is publicly available). If the server naively uses the `alg` header to decide verification method, it verifies HMAC with the public key — which succeeds!

**Fix:**
```java
// NEVER trust the alg header from the token
@Bean
public JwtDecoder jwtDecoder() {
    // Explicitly specify the algorithm — ignore what the token says
    return NimbusJwtDecoder.withPublicKey(rsaPublicKey)
        .signatureAlgorithm(SignatureAlgorithm.RS256) // FIXED algorithm
        .build();
}
```

**Additional protections:**
- Reject tokens with `alg: none` (unsigned tokens)
- Validate `iss` (issuer) and `aud` (audience) claims
- Use a well-tested library (Nimbus, jjwt) that handles these by default
- Keep signing keys rotated and stored securely (Vault, K8s secrets)

---

## Practice Task

Build a Spring Boot REST API with:
1. JWT authentication (login endpoint returns token)
2. Role-based authorization (USER vs ADMIN endpoints)
3. Custom JWT filter
4. CORS configuration
5. Password hashing with BCrypt

→ See code in `spring-boot-examples/src/main/java/com/interview/springboot/security/`

---

## Code Examples

| File | Topics |
|------|--------|
| [SecurityConfig.java](../spring-boot-examples/src/main/java/com/interview/springboot/security/SecurityConfig.java) | Filter chain, CORS, CSRF, endpoint authorization |
| [JwtTokenProvider.java](../spring-boot-examples/src/main/java/com/interview/springboot/security/JwtTokenProvider.java) | JWT generation, validation, claims extraction |
| [JwtAuthFilter.java](../spring-boot-examples/src/main/java/com/interview/springboot/security/JwtAuthFilter.java) | Custom filter that validates JWT on every request |
