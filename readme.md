# Document Analyzer

A Spring Boot application to upload documents and ask questions about their content. It splits documents into chunks, stores vector embeddings in PostgreSQL (pgvector) and uses OpenAI for chat answers. Caching and performance monitoring are enabled.

## Features
- Upload `.txt` documents and automatic chunking
- Semantic search over chunks (cosine similarity via pgvector)
- GPT‑based answering with configurable prompts
- JWT authentication (register/login + Bearer token)
- Caffeine caching (default/embedding/quick) and admin endpoints
- Metrics via Micrometer + Actuator, Prometheus export
- Swagger UI and generated OpenAPI specification

## Getting Started
1. Prerequisites
   - Java 21+
   - PostgreSQL with the `vector` extension
   - OpenAI API key (for chat) as env var
2. Database setup
   ```sql
   CREATE DATABASE document_analyser;
   \c document_analyser
   CREATE EXTENSION IF NOT EXISTS vector;
   ```
3. Configuration
   The project uses `src/main/resources/application.properties`.

   Recommended to set sensitive values via environment variables:
   - `SPRING_DATASOURCE_URL` (e.g., `jdbc:postgresql://localhost:5432/document_analyser`)
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_AI_OPENAI_API_KEY`

   Notes:
   - By default, embeddings are generated locally (deterministic) via `EmbeddingConfig` for simplicity in dev/demo. To use OpenAI embeddings, replace the `EmbeddingClient` bean accordingly.
   - The embedding vector size is 1536 and must match the pgvector column definition.

4. Run
   ```bash
   ./mvnw spring-boot:run
   # or
   mvn spring-boot:run
   ```
5. Swagger
   Open: http://localhost:8080/swagger-ui/index.html

## Authentication
- Register: `POST /auth/register`
- Login: `POST /auth/login` → returns JWT
- Use the token for protected endpoints with header `Authorization: Bearer <token>`

Example (curl):
```bash
# Register (role defaults to USER)
curl -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"user1","password":"pass123"}'

# Login → get token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user1","password":"pass123"}' | jq -r .token)

# Upload document (protected)
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer $TOKEN" \
  -F file=@sample-data/legal_demo_contract_bg.txt

# Ask a question via demo endpoint (protected)
curl -X POST http://localhost:8080/api/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: text/plain' \
  --data 'Какъв е срокът на договора?'
```

## Caching & Performance
- Caching: Caffeine with three managers
  - Default (`questions`, `documents`, `users`)
  - Embedding (longer TTL: `embeddings`)
  - Quick (short TTL: `relevantChunks`, `documentChunks`, `chunkStats`, `questionHistory`)
- Cache admin endpoints (protected):
  - `POST /api/admin/cache/clear-all`
  - `POST /api/admin/cache/clear/{cacheName}`
  - `GET /api/admin/cache/stats`, `GET /api/admin/cache/names`
- Performance endpoints (protected):
  - `GET /api/admin/performance/stats`, `/cache-metrics`, `/database-metrics`, `/response-times`, `/response-analysis`
  - Benchmarks/tests: `/api/admin/performance/database-test`, `/cache-test`, `/concurrent-requests`, `/memory-usage`, `/gc-test`, `/benchmark`
- Actuator/Prometheus:
  - `GET /actuator/health`, `/actuator/info`, `/actuator/prometheus`

## Architecture
- Controllers: document upload (`POST /api/documents`), chat demo (`POST /api/ask`), auth, cache/performance admin
- Services: document processing (chunk + embeddings), vector search, question answering, caching, logging/metrics
- Repositories: JPA + pgvector queries and projections for performance

OpenAPI is generated during build to `target/openapi.json` via the Maven plugin.

## Security note
Do not commit API keys or database passwords. Prefer environment variables or externalized configuration.
