# 🤖 Intelligent Document Assistant

This is a Spring Boot application that allows users to upload internal documents, ask natural language questions about them, and receive context-aware answers powered by OpenAI (GPT-4). The project uses Spring AI for integration with OpenAI and pgvector + PostgreSQL for semantic search via embeddings.

---

## 🚀 Features

- Upload documents (TXT, PDF, DOCX)
- Extract content and generate embeddings
- Store document content and metadata in PostgreSQL
- Store embeddings in pgvector (vector store)
- Ask natural language questions
- Semantic similarity search for relevant content
- GPT-4 generated answers based on retrieved context
- History of all Q&A interactions
- RESTful API + optional web interface (Thymeleaf)

---

## 🧱 Technologies Used

| Layer           | Stack                                        |
|----------------|----------------------------------------------|
| Backend         | Spring Boot, Spring Web, Spring AI           |
| AI Integration  | OpenAI (via Spring AI)                       |
| Vector Search   | pgvector + PostgreSQL                        |
| Data Persistence| Spring Data JPA                              |
| Security        | (optional) Spring Security + JWT             |
| UI              | (optional) Thymeleaf / REST API + Postman    |
| Build Tool      | Maven                                        |

---