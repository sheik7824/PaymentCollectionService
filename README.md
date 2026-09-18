# Payment Collection Service

Minimal, production-shaped Spring Boot backend for JWT-authenticated payment collection.

## Stack

Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA · PostgreSQL 15 · Flyway · Spring Retry · springdoc-openapi · Actuator · JUnit 5 / Mockito · Docker

## Architecture

```
Controller -> Service -> Gateway Adapter (retry) -> Repository -> PostgreSQL
```

| Package | Responsibility |
|---|---|
| `auth` | `POST /api/v1/auth/login` → JWT |
| `security` | JWT issue/verify, stateless filter chain, BCrypt |
| `payment` | Payment orchestration, transaction persistence, retrieval |
| `payment.idempotency` | `Idempotency-Key` handling (SHA-256 request fingerprint) |
| `gateway` | `PaymentGateway` port + `Mock`/`Razorpay`/`Stripe` adapters, `RetryTemplate` (3 attempts, exponential backoff) |
| `audit` | Append-only `audit_logs` writes in their own transaction |
| `common.exception` | Global `@RestControllerAdvice` → uniform `ApiError` |
| `config` | Correlation-ID filter (`X-Correlation-Id` → MDC), OpenAPI |

## Run

```bash
# Postgres + app
docker compose up --build

# or locally against a running Postgres
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Swagger UI: http://localhost:8080/swagger-ui.html · Health: http://localhost:8080/actuator/health

Seeded users (Flyway `V2__seed_users.sql`): `admin/admin123` (ADMIN), `consumer/consumer123` (API_CONSUMER).

## API

```bash
TOKEN=$(curl -s localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"consumer","password":"consumer123"}' | jq -r .accessToken)

curl -s localhost:8080/api/v1/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Idempotency-Key: abc-123-xyz' \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-10001","amount":1500.00,"currency":"INR","customerReference":"CUST-101"}'

curl -s localhost:8080/api/v1/payments/TXN-XXXX -H "Authorization: Bearer $TOKEN"
curl -s localhost:8080/api/v1/payments/order/ORD-10001 -H "Authorization: Bearer $TOKEN"
```

### Mock gateway behaviour

| Amount | Result |
|---|---|
| ends in `.99` | `FAILED` (Insufficient funds) |
| ends in `.50` | `PENDING` |
| exactly `999` | throws → retried 3× → `FAILED` (Gateway error) |
| anything else | `SUCCESS` |

Switch provider via `PAYMENT_GATEWAY=mock|razorpay|stripe`.

## Configuration

| Env var | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/payments` |
| `DB_USERNAME` / `DB_PASSWORD` | `payments` / `payments` |
| `JWT_SECRET` | dev placeholder (≥ 32 bytes; **override in prod**) |
| `JWT_EXPIRATION_SECONDS` | `3600` |
| `PAYMENT_GATEWAY` | `mock` |

## Tests

```bash
mvn test
```

- `PaymentServiceTest` – unit tests (Mockito): success/decline/retry/idempotency/authorization paths.
- `PaymentFlowIntegrationTest` – `@SpringBootTest` + MockMvc on H2: login → payment → replay → conflict → retrieval.
