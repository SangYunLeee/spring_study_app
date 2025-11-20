# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Specification-First Development** learning project using Spring Boot. The core principle: **specifications come before code**.

**Architecture:**
```
OpenAPI Spec → Code Generation → Controller Implementation
                                      ↓
                                API ↔ Domain Conversion
                                      ↓
                                Service (Business Logic)
                                      ↓
                                Repository (JPA)
                                      ↓
dbmate Spec → DB Schema → PostgreSQL
```

## Essential Development Commands

**IMPORTANT**: Use `/usr/local/bin/docker-compose` for docker-compose commands (NOT `docker compose`).

### Database Management

```bash
# Start PostgreSQL + auto-migration
/usr/local/bin/docker-compose up -d

# Manual migration (if needed)
/usr/local/bin/docker-compose run --rm dbmate up

# Check migration status
/usr/local/bin/docker-compose run --rm dbmate status

# Create new migration (manual - timestamp required)
# File format: database/db/migrations/YYYYMMDDHHMMSS_description.sql

# Connect to database
/usr/local/bin/docker exec -it springbasic-postgres psql -U springuser -d springbasic

# Check container status
/usr/local/bin/docker-compose ps
```

### API Development

```bash
# Generate API code from OpenAPI spec (REQUIRED before build)
./gradlew generateApi

# Clean build
./gradlew clean build

# Run application
./gradlew bootRun

# Run tests
./gradlew test
```

### Development Workflow

**When changing API:**
1. Edit OpenAPI spec: `src/main/resources/openapi/api-spec.yaml`
2. Generate: `./gradlew generateApi`
3. Update Controller implementation
4. Test

**When changing DB schema:**
1. Create migration: `cd database && dbmate new <description>`
2. Write SQL in `database/db/migrations/xxx.sql`
3. **Update DBML schema**: `database/db/schema.dbml` (keep in sync!)
4. Run: `/usr/local/bin/docker-compose run --rm dbmate up`
5. Update Entity: `src/main/java/.../model/*.java`
6. Start app (JPA validates schema automatically)

**IMPORTANT**: DBML과 migration은 항상 함께 업데이트해야 합니다!

## High-Level Architecture

### Specification-First Philosophy

This project uses a **contract-first** approach where specifications are the source of truth:

- **API Layer**: Defined in OpenAPI 3.0.3, code auto-generated via OpenAPI Generator
- **Database Layer**: Defined in SQL migrations (dbmate), tracked in Git
- **Domain Layer**: Hand-written entities and business logic

**Critical Rule**: Never modify generated code. Always change the spec and regenerate.

### Layer Separation

```
┌─────────────────────────────────────┐
│ API Layer (Controller)              │
│ - Auto-generated DTOs               │
│ - CreateUserRequestDto              │
│ - UserResponseDto                   │
└─────────────────────────────────────┘
            ↕ Conversion
┌─────────────────────────────────────┐
│ Domain Layer (Service, Repository)  │
│ - Hand-written entities             │
│ - User Entity                       │
│ - Business logic                    │
└─────────────────────────────────────┘
```

**Why separate?**
- API changes don't affect business logic
- Same Service can be reused by different APIs (REST, GraphQL, etc.)
- Easier testing
- See: `docs/WHY_NOT_API_MODEL_IN_SERVICE.md`

### Database Independence

DB migrations run **independently** from the application:

1. dbmate manages schema changes (Docker container or local CLI)
2. Spring Boot validates schema on startup (`ddl-auto: validate`)
3. Mismatch = startup failure (safety mechanism)

**Never use** `ddl-auto: create` or `update` in production. Always use dbmate migrations.

### Generated Code Location

- API Interfaces & DTOs: `build/generated/src/main/java/com/example/springbasic/api/`
- QueryDSL Q-Types: `build/generated/sources/annotationProcessor/java/main/`

These are in `.gitignore`. Regenerate with `./gradlew generateApi`.

## Key Design Patterns

### OpenAPI Modular Structure

OpenAPI spec is split into multiple files using `$ref`:

```
src/main/resources/openapi/
├── api-spec.yaml           # Main spec (entry point)
├── paths/                  # Endpoint definitions
│   ├── users-base.yaml
│   ├── users-by-id.yaml
│   └── users-search.yaml
└── schemas/                # Request/Response schemas
    ├── CreateUserRequest.yaml
    ├── UserResponse.yaml
    └── ErrorResponse.yaml
```

When adding new endpoints, create separate files and reference them in `api-spec.yaml`.

### Dependency Injection

**Always use constructor injection** (never field injection):

```java
@Service
public class UserService {
    private final UserRepository repository;  // final is crucial

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

### Exception Handling

Global exception handling via `@RestControllerAdvice`:

- Custom exceptions: `exception/UserNotFoundException.java`
- Global handler: `exception/GlobalExceptionHandler.java`
- Never use try-catch in Controllers

### Transaction Management

- Write operations: `@Transactional`
- Read operations: `@Transactional(readOnly = true)`
- Always at Service layer, never Controller or Repository

### JPA Best Practices

1. **Always use LAZY fetching** (default for @ManyToOne)
2. **Solve N+1 with Fetch Join**: Use `@Query` with `JOIN FETCH`
3. **Use QueryDSL for complex queries**: Type-safe, compile-time checked
4. **Pagination**: Use Spring Data JPA's `Pageable`

## File Organization

### Controllers
- Implement generated API interfaces (e.g., `UsersApi`)
- Only handle HTTP concerns (request/response)
- Convert between API DTOs and Domain models
- No business logic

### Services
- Business logic only
- Use Domain models (Entity), never API DTOs
- Transactional methods
- Return Domain models

### Repositories
- Extend `JpaRepository<Entity, ID>`
- Custom queries: Use QueryDSL via `Custom` interface pattern
- No business logic

### DTOs
- Auto-generated from OpenAPI spec
- Request: `CreateUserRequestDto`, `UpdateUserRequestDto`
- Response: `UserResponseDto`
- Never modify manually (regenerate from spec)

## Testing Strategy

### Unit Tests
- Service: Mock Repository with `@Mock` and `@InjectMocks`
- Controller: Use `@WebMvcTest` and `MockMvc`
- Fast, isolated, no DB

### Integration Tests
- Use `@SpringBootTest`
- Real database (configured in `src/test/resources/application.yml`)
- Test full stack
- Slower but comprehensive

### Test Naming
```java
@Test
@DisplayName("사용자 생성 - 성공")
void createUser_Success() { ... }

@Test
@DisplayName("사용자 생성 - 중복 이메일로 실패")
void createUser_DuplicateEmail_ThrowsException() { ... }
```

## Configuration Files

### application.yml (Production)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/springbasic
  jpa:
    hibernate:
      ddl-auto: validate  # NEVER change this!
```

### application-dev.yml (Development)
- `show-sql: true` for query logging
- More verbose logging

### application-test.yml (Testing)
- Separate test database
- `ddl-auto: create-drop` only for tests

## Migration Files

Located in `database/db/migrations/`:

**Naming**: `YYYYMMDDHHMMSS_description.sql`

**Structure**:
```sql
-- migrate:up
CREATE TABLE users (...);

-- migrate:down
DROP TABLE IF EXISTS users;
```

**Rules**:
- Never modify executed migrations
- Always include both up and down
- Sequential timestamps

## Common Pitfalls

1. **Don't modify generated code**: Change the spec and regenerate
2. **Don't use API DTOs in Service**: Use Domain entities
3. **Don't skip `generateApi` after spec changes**: Build will fail
4. **Don't use `ddl-auto: create`**: Use dbmate migrations
5. **Don't use EAGER fetching**: Causes N+1 problems
6. **Don't forget @Transactional**: Data inconsistency risk

## Quick Reference

### Add New Entity
1. Create migration: `dbmate new create_<table>_table`
2. Write SQL with constraints
3. Run: `dbmate up`
4. Create Entity: `@Entity` class with JPA annotations
5. Create Repository: `JpaRepository<Entity, Long>`
6. Create Service with `@Transactional`

### Add New API Endpoint
1. Create schema YAML: `schemas/NewRequest.yaml`
2. Create path YAML: `paths/new-endpoint.yaml`
3. Reference in `api-spec.yaml`
4. Generate: `./gradlew generateApi`
5. Implement in Controller
6. Add Service method if needed

### Fix Schema Mismatch Error
1. Check error message for missing column/table
2. Create migration: `dbmate new fix_<issue>`
3. Add missing column/table
4. Update Entity annotations
5. Run: `dbmate up`
6. Restart app

## Documentation

Essential reading (in `docs/`):
- `SESSION_CONTEXT.md` - Quick context for new sessions
- `WHY_NOT_API_MODEL_IN_SERVICE.md` - Layer separation rationale
- `DB_SPEC_MANAGEMENT.md` - dbmate usage guide
- `LEARNING_LOG.md` - Full learning history (13 sessions)

## Learning Context

This is an educational project. When assisting:
1. Explain **why**, not just **what**
2. Use Korean comments in code
3. Update `LEARNING_LOG.md` after major changes
4. Document new concepts in `docs/`
5. Prefer modern Java features (records for DTOs, var, streams)
