# Document Analyzer Setup Guide

## Prerequisites

1. **PostgreSQL Database** with pgvector extension
2. **OpenAI API Key** for embeddings
3. **Java 21** and Maven

## Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE document_analyser;
```

2. Enable the pgvector extension:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## Configuration

1. Set your OpenAI API key as an environment variable:
```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

2. Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/document_analyser
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Running the Application

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

## API Endpoints

### Upload Document
- **POST** `/api/documents`
- **Content-Type**: `multipart/form-data`
- **Parameter**: `file` (currently supports .txt files only)

Example using curl:
```bash
curl -X POST -F "file=@document.txt" http://localhost:8080/api/documents
```

### Ask Question
- **POST** `/ask`
- **Content-Type**: `text/plain`
- **Body**: Your question

Example using curl:
```bash
curl -X POST -H "Content-Type: text/plain" -d "What is this document about?" http://localhost:8080/ask
```

## Current Features

✅ Document upload and storage in PostgreSQL
✅ Text extraction from .txt files
✅ JPA entities for Document, Question, and Answer
✅ Basic chat functionality with dummy responses
✅ Database configuration with pgvector extension

## TODO Features

- [ ] PDF and DOCX support using Apache Tika
- [ ] Document chunking for large files
- [ ] Embedding generation and vector storage
- [ ] Similarity search implementation
- [ ] Enhanced question-answering with document context

## Project Structure

```
src/main/java/com/example/Document_analiser/
├── DocumentAnaliserApplication.java    # Main application class
├── ChatClient.java                     # Chat client implementation
├── ChatController.java                 # Chat REST controller
├── controller/
│   └── DocumentController.java         # Document upload controller
├── service/
│   └── DocumentService.java            # Document processing service
├── entity/
│   ├── Document.java                   # Document entity
│   ├── Question.java                   # Question entity
│   └── Answer.java                     # Answer entity
├── repository/
│   ├── DocumentRepository.java         # Document repository
│   ├── QuestionRepository.java         # Question repository
│   └── AnswerRepository.java           # Answer repository
├── dto/
│   ├── QuestionRequest.java            # Question request DTO
│   └── AnswerResponse.java             # Answer response DTO
└── config/
    └── DatabaseConfig.java             # Database configuration
``` 