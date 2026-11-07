# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Spring Boot 4.1 / Java 17 REST API (Maven, Lombok, JPA, Bean Validation) for a Tortas Ahogadas restaurant point of sale: orders made of per-customer sub-orders, products/inventory, and money transactions. No auth yet.

## Commands

Use the Maven wrapper (`./mvnw` in Git Bash, `.\mvnw.cmd` in PowerShell).

- Build: `./mvnw compile`
- Run: `./mvnw spring-boot:run` (needs a database, see Config)
- Unit tests: `./mvnw test -Dtest='*ServiceTest'`
- Single test: `./mvnw test -Dtest=OrderServiceTest#cancelingSubOrderRestoresStockAndLowersTotal`
- `./mvnw test` with no filter also runs `TtBackendApplicationTests.contextLoads`, which needs a live database and `DB_PASSWORD`. Use the `*ServiceTest` filter when no DB is available.

There is no linter or formatter configured.

## Config

The database is MySQL. `src/main/resources/application.properties` imports an optional, gitignored `.env` file at the repo root, so it is only found when the app runs from the root. It reads `DB_URL` (optional), `DB_USERNAME` (optional) and `DB_PASSWORD` (required) from that file or from real env vars. Never commit credentials. Schema is managed by `spring.jpa.hibernate.ddl-auto=update`. It adds columns but never changes column types or adds non-null FKs to existing rows. DB data is disposable test data, so the usual fix is dropping and recreating tables.

## Architecture

Layered: `controller` → `service` → `repository` (Spring Data JPA), with `model` entities and `model/dto` request/response DTOs. Controllers stay thin, and business rules live in services.

- **Errors:** services throw `HttpClientErrorException(status, message)`. `exception/GlobalExceptionHandler` turns it into `{status, message}` JSON and also formats `@Valid` failures. Missing records throw 404 rather than returning null.
- **Order aggregate:** `Order` → `SubOrder` (one per customer, has its own status) → `SubOrderItems` (product, quantity, price snapshot, notes). Cascades go from `Order`, so save via `orderRepository.save(order)`. Back-references use `@ToString.Exclude`/`@EqualsAndHashCode.Exclude` to avoid Lombok `@Data` recursion.
- **Status rules (in `OrderService`):** order and sub-order statuses only move out of `PENDING`, and `COMPLETED`/`CANCELED` are final.
  - An order completes only when every non-canceled sub-order is `COMPLETED`, and at least one is.
  - Canceling a sub-order restores its stock and subtracts its subtotal from the order total.
  - Canceling an order restores stock for every sub-order not already canceled.
- **Stock:** decremented in `createOrder` and restored in `restoreStock`. Products are soft-deleted (`active=false`) because sold items reference them.
- **Money:** `BigDecimal` with `@Column(precision = 10, scale = 2)`. Enums are stored with `@Enumerated(EnumType.STRING)`.
- **Tests:** Mockito unit tests of services (`@ExtendWith(MockitoExtension.class)`, `@InjectMocks`), with entities built in memory. See `src/test/.../service/OrderServiceTest.java`.

## Roadmap and workflow

Work is incremental: one `feature/*` branch per step, merged to `main` before the next. Planned order: payments (per sub-order, at completion) → transactions (outcomes) → cash-session (single open register, expected cash) → inventory (stock movement log) → auth (JWT, roles ADMIN/CASHIER) → reports. Don't pull a later step's features into the current branch.

Planned permissions: ADMIN only opens or closes sessions, cancels orders and sub-orders, registers outcomes, manages products, prices and users, and sees reports. CASHIER creates and completes orders, adjusts inventory and sees the expected cash.

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
