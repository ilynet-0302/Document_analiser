# Document Analyzer Setup Guide

## Prerequisites

- Java 21+
- PostgreSQL 12+ with the `pgvector` extension
- OpenAI API key for chat (optional for embeddings; by default embeddings are local)
- Maven 3.6+ (or use the provided wrapper `mvnw`/`mvnw.cmd`)

## 1) Database

Create the database and enable pgvector:

```sql
CREATE DATABASE document_analyser;
\c document_analyser
CREATE EXTENSION IF NOT EXISTS vector;
```

## 2) Configuration

Use environment variables for sensitive values (recommended):

Linux/macOS (bash/zsh):
```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/document_analyser"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="<your_password>"
export SPRING_AI_OPENAI_API_KEY="<your_openai_api_key>"
```

Windows PowerShell:
```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/document_analyser"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "<your_password>"
$env:SPRING_AI_OPENAI_API_KEY = "<your_openai_api_key>"
```

Alternatively, edit `src/main/resources/application.properties` and set datasource and OpenAI settings. Avoid committing secrets.

Notes:
- By default, embeddings are generated locally (deterministic) via `EmbeddingConfig`. To use OpenAI embeddings, replace the `EmbeddingClient` bean accordingly.
- Embedding vector dimension is 1536 and must match the pgvector column.

## 3) Build & Run

```bash
./mvnw clean install
./mvnw spring-boot:run
# or with Maven
mvn clean install
mvn spring-boot:run
```

After start:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Actuator: http://localhost:8080/actuator

## 4) Authentication

Register and login via JSON to obtain a JWT token:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass123"}'

curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass123"}'
# Use the returned token in: Authorization: Bearer <token>
```

The UI endpoints require authentication. Form login is enabled (`/login`).

## 5) API Endpoints (high level)

- Upload Document: `POST /api/documents` (multipart form, param `file`)
- Ask Question (document-aware): `POST /api/questions` (JSON: `{ text, documentId }`)
- Question History: `GET /api/questions/history?documentId={id}&order=asc|desc&page={n}`
- Demo Chat (system prompt + user): `POST /api/ask` (text/plain body)

Admin (protected):
- Cache: `POST /api/admin/cache/clear-all`, `POST /api/admin/cache/clear/{cacheName}`, `GET /api/admin/cache/stats`, `GET /api/admin/cache/names`
- Performance: `GET /api/admin/performance/stats`, `/cache-metrics`, `/database-metrics`, `/response-times`, `/response-analysis`
- Benchmarks/Tests: `POST /api/admin/performance/database-test`, `/cache-test`, `/concurrent-requests`, `GET /api/admin/performance/memory-usage`, `POST /api/admin/performance/gc-test`, `POST /api/admin/performance/benchmark`

## 6) Web UI

- `/` Home
- `/ask` Ask a question about a selected document
- `/upload` Upload a `.txt` document (size validated)
- `/history` View your previous questions/answers

## 7) Project Structure (key parts)

```
src/main/java/com/example/Document_analiser/
  DocumentAnaliserApplication.java
  controller/
    AuthController.java
    UiController.java
    QuestionController.java
    PublicQuestionController.java
    DocumentController.java
    CacheController.java
    PerformanceController.java
  service/
    DocumentService.java
    QuestionService.java
    VectorSearchService.java
    AuthService.java
    CustomUserDetailsService.java
    ResponseTimeAnalyzer.java
    LogAnalysisService.java
  repository/
    UserRepository.java
    DocumentRepository.java
    DocumentChunkRepository.java
    QuestionRepository.java
  config/
    SecurityConfig.java
    CacheConfig.java
    EmbeddingConfig.java
    WebConfig.java
resources/templates/
  layout.html, home.html, ask.html, upload.html, history.html, fragments/answer.html
```

## 8) Next Steps / Options

- Enable OpenAI embeddings (replace local `EmbeddingClient`)
- Add more extractors (PDF/DOCX via Apache Tika)
- Tune cache TTLs and sizes for your workload
- Expose Prometheus metrics and build dashboards

