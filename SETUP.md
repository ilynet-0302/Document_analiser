# Document Analyzer Setup Guide

## Prerequisites

- **PostgreSQL 12+** with the pgvector extension
- **OpenAI API key** for embeddings and chat completion
- **Java 21** and **Maven 3.6+**

## 1. Database

Create the database and enable pgvector:

```sql
CREATE DATABASE document_analyser;
\c document_analyser
CREATE EXTENSION IF NOT EXISTS vector;
```

## 2. Configuration

Set your OpenAI API key and database credentials.

```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/document_analyser
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Visit [http://localhost:8080](http://localhost:8080) and log in to access the UI.

## 4. Authentication

Use the JSON endpoints to register and obtain a JWT token:

```bash
curl -X POST -H "Content-Type: application/json" -d '{"username":"alice","password":"pass123"}' http://localhost:8080/auth/register
curl -X POST -H "Content-Type: application/json" -d '{"username":"alice","password":"pass123"}' http://localhost:8080/auth/login
# Use the returned token as: Authorization: Bearer <token>
```

UI pages (`/ask`, `/upload`, `/history`) require login. The REST API also expects the JWT in the `Authorization` header.

## 5. API Endpoints

### Upload Document
- `POST /api/documents` (multipart/form-data, param: `file`)

### Ask Question
- `POST /api/questions` (JSON: `text`, `documentId`)

### Question History
- `GET /api/questions/history?documentId={id}&order=asc|desc&page={n}`

## 6. Web UI

- **/ask** – Ask a question about a selected document
- **/upload** – Upload a `.txt` document (size-limited client and server validation)
- **/history** – Paginated table of your previous questions and answers with document filtering

## 7. Project Structure

```
src/main/java/com/example/Document_analiser/
├── DocumentAnaliserApplication.java    # Main application class
├── controller/
│   ├── AuthController.java            # Register/login endpoints
│   ├── UiController.java              # Thymeleaf pages
│   ├── QuestionController.java        # REST API for questions
│   ├── PublicQuestionController.java  # Public Q&A API
│   └── DocumentController.java        # REST API for documents
├── service/
│   ├── QuestionService.java
│   ├── DocumentService.java
│   ├── AuthService.java
│   └── CustomUserDetailsService.java
├── entity/
│   ├── User.java, Role.java
│   ├── Document.java
│   ├── Question.java
│   └── Answer.java
├── repository/
│   ├── UserRepository.java
│   ├── DocumentRepository.java
│   ├── QuestionRepository.java
│   └── AnswerRepository.java
├── config/
│   ├── SecurityConfig.java, DatabaseConfig.java
└── resources/templates/
    ├── layout.html, home.html
    ├── ask.html, upload.html
    ├── history.html
    └── fragments/answer.html
```

## 8. Next Steps

- PDF and DOCX support via Apache Tika
- Document chunking and embedding storage
- Semantic similarity search for better context
- Richer answer editing and moderation tools