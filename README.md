# CodeCollab

## 📖 What is this?

CodeCollab is a developer collaboration platform where engineers save, search, analyze, and discuss code snippets with real-time chat. Built with Java 17 + Spring Boot 3, it combines a rich REST API, WebSocket-based live collaboration, and AST-powered code analysis into a single cohesive desktop-first experience.

---

## 🎯 Current Status: Phase 0 — Foundation

| Phase | Name | Status |
|-------|------|--------|
| ✅ **Phase 0** | **Foundation & Setup** | **Complete** |
| ⬜ Phase 1 | User Auth (JWT + Spring Security) | Pending |
| ⬜ Phase 2 | Snippet CRUD & Full-Text Search | Pending |
| ⬜ Phase 3 | Caching Layer (Redis) | Pending |
| ⬜ Phase 4 | Real-Time Chat (WebSocket + STOMP) | Pending |
| ⬜ Phase 5 | AST Analysis (JavaParser) | Pending |
| ⬜ Phase 6 | Presence & Collaboration | Pending |
| ⬜ Phase 7 | Desktop UI (JavaFX / Electron) | Pending |

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Core language (records, text blocks) |
| Spring Boot | 3.2.5 | Application framework |
| Maven | 3.9+ | Build tool |
| PostgreSQL | 16 | Primary database |
| Redis | 7 | Cache & real-time presence |
| Flyway | (managed by Boot) | DB schema migrations |
| Hibernate / JPA | (managed by Boot) | ORM layer |
| Lombok | (managed by Boot) | Boilerplate reduction |
| Spring Actuator | (managed by Boot) | Health & metrics |
| springdoc-openapi | 2.3.0 | Swagger UI |
| Docker Compose | v2+ | Local infrastructure |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** — `java -version`
- **Maven 3.9+** — `mvn -version`
- **Docker & Docker Compose v2+** — `docker compose version`

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/your-org/codecollab.git
cd codecollab

# 2. Start infrastructure (Postgres + Redis)
docker compose up -d

# 3. Wait for containers to become healthy (~10 seconds)
docker compose ps   # both services should show "healthy"

# 4. Build the project
mvn clean install -DskipTests

# 5. Run the application
mvn spring-boot:run
```

### Verify Everything Works

| URL | Expected Result |
|-----|----------------|
| http://localhost:8080/api/health | `{"success":true,"data":{"status":"UP",...}}` |
| http://localhost:8080/api/health/db | `{"success":true,"message":"Database connection OK",...}` |
| http://localhost:8080/api/health/redis | `{"success":true,"message":"Redis connection OK",...}` |
| http://localhost:8080/swagger-ui.html | Swagger UI with CodeCollab API |
| http://localhost:8080/actuator/health | `{"status":"UP"}` |

### Stopping

```bash
# Stop the Spring Boot app
Ctrl+C

# Stop Docker containers (preserves data volumes)
docker compose down

# Stop AND delete all data volumes (clean slate)
docker compose down -v
```

---

## 📁 Project Structure

```
codecollab/
├── .gitignore
├── README.md
├── docker-compose.yml
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/codecollab/
    │   │       ├── CodeCollabApplication.java
    │   │       ├── config/
    │   │       │   ├── OpenApiConfig.java      ← Swagger UI setup
    │   │       │   ├── RedisConfig.java        ← RedisTemplate bean
    │   │       │   └── SecurityConfig.java     ← Permissive for Phase 0
    │   │       ├── common/
    │   │       │   ├── ApiResponse.java        ← Uniform API envelope (record)
    │   │       │   └── GlobalExceptionHandler.java
    │   │       ├── health/
    │   │       │   └── HealthController.java   ← /api/health, /db, /redis
    │   │       ├── user/           ← Phase 1
    │   │       ├── snippet/        ← Phase 2
    │   │       ├── chat/           ← Phase 4
    │   │       └── ast/            ← Phase 5
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    │           └── V1__init_marker.sql
    └── test/
        └── java/com/codecollab/
            ├── CodeCollabApplicationTests.java
            └── health/
                └── HealthControllerTest.java
```

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=HealthControllerTest

# Run with verbose output
mvn test -Dsurefire.useFile=false
```

Tests use `@WebMvcTest` slices — **no running Docker containers required**.

---

## 🗺️ Roadmap

- [x] Phase 0 — Foundation & Setup
- [ ] Phase 1 — User Auth (JWT + Spring Security)
- [ ] Phase 2 — Snippet CRUD & Full-Text Search
- [ ] Phase 3 — Caching Layer (Redis)
- [ ] Phase 4 — Real-Time Chat (WebSocket + STOMP)
- [ ] Phase 5 — AST Analysis (JavaParser)
- [ ] Phase 6 — Presence & Collaboration
- [ ] Phase 7 — Desktop UI (JavaFX / Electron)

---

## 🔧 Troubleshooting

### Port 5432 already in use
```bash
# Find and kill the process using the port
netstat -ano | findstr :5432          # Windows
lsof -i :5432 | awk 'NR>1 {print $2}' | xargs kill  # macOS/Linux

# Or change the port in docker-compose.yml: "5433:5432"
# Then update application-dev.yml datasource URL to port 5433
```

### Port 6379 already in use
```bash
netstat -ano | findstr :6379          # Windows
lsof -i :6379                         # macOS/Linux
# Same approach — change "6380:6379" in docker-compose.yml + application.yml
```

### Port 8080 already in use
```bash
# Change server.port in application.yml to 8081 (or any free port)
```

### Flyway migration fails
```bash
# 1. Check Flyway migration table
docker exec -it codecollab-postgres psql -U codecollab -d codecollab -c "SELECT * FROM flyway_schema_history;"

# 2. If schema is in a bad state (dev only!), wipe and restart
docker compose down -v
docker compose up -d
mvn spring-boot:run
```

### Redis connection refused
```bash
# Verify Redis is running and healthy
docker compose ps
docker exec -it codecollab-redis redis-cli ping   # should return PONG

# Check application.yml redis host/port matches docker-compose.yml
```

### Verify Flyway tables
```bash
docker exec -it codecollab-postgres psql -U codecollab -d codecollab -c "\dt"
# Expected: schema_marker + flyway_schema_history
```

---

## 📝 License

MIT — see [LICENSE](LICENSE) for details.
