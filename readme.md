# Document Analyzer

A Spring Boot application that lets users upload documents and ask questions about their content. It splits documents into semantic chunks, stores vector embeddings in PostgreSQL with pgvector and uses OpenAI to generate context-aware answers.

## Features
- Upload `.txt` documents and automatically chunk them
- Semantic search over document chunks using cosine similarity
- GPT‑based question answering with configurable prompts
- Swagger UI and generated OpenAPI specification

## Getting Started
1. **Prerequisites**
   - Java 21+
   - PostgreSQL with the `vector` extension
   - OpenAI API key exported as `OPENAI_API_KEY`
2. **Database setup**
   ```sql
   CREATE DATABASE document_analyser;
   \c document_analyser
   CREATE EXTENSION IF NOT EXISTS vector;
   ```
3. **Configuration**
   Edit `src/main/resources/application.yml` with your datasource credentials and OpenAI settings.
4. **Run**
   ```bash
   mvn spring-boot:run
   ```
5. **Swagger**
   After the application starts, navigate to [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for interactive API docs.

## Architecture
- **Controller layer**: REST endpoints for documents and questions
- **Service layer**: business logic, chunking and semantic search
- **Repository layer**: Spring Data JPA with pgvector support

The project also includes a Maven plugin to generate `openapi.json` during the build for integration with other tools.