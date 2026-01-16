# In-memory Authentication Service (Backend)

Backend-only login/auth service using Java 21, Spring Boot 4.0.1, Spring Security (JWT), and Maven. Data is held in memory; no database required.

## Quick start

Prereqs: Java 21, Maven 3.9+.

```bash
mvn clean verify
mvn spring-boot:run
```

## Default users

| username | password  | roles          |
|----------|-----------|----------------|
| admin    | admin123  | ROLE_ADMIN, ROLE_USER |
| user     | user123   | ROLE_USER      |
| viewer   | viewer123 | ROLE_VIEWER    |

Passwords are BCrypt-hashed in memory.

## Response envelope

All endpoints return JSON shaped as `{ "code": <int>, "message": <string>, "data": <object|null> }`. HTTP status is always 200; use the `code` field to determine success (`200`) or failure (non-200). Controllers return domain objects directly—wrapping is handled by a global advice.

## Endpoints

- `POST /api/auth/login` — authenticate with `{ "username": "user", "password": "user123" }`. Returns username, roles, a JWT token, and a confirmation message.
- `GET /api/users/me` — returns the authenticated principal (`username`, `roles`). Requires `Authorization: Bearer <token>` from the login response.

### cURL examples

```bash
# Login (returns token in data.token)
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# Access protected resource with Bearer token
TOKEN="<paste token>"
curl -i http://localhost:8080/api/users/me \
  -H "Authorization: Bearer ${TOKEN}"
```

## Notes

- CSRF is disabled for API convenience; sessions are stateless.
- Adjust in-memory users/roles in `SecurityConfig` as needed.
