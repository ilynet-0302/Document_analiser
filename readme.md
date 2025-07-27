# 📄 Document Analyzer

A full-stack Spring Boot application for intelligent document analysis. Upload documents (TXT, PDF, DOCX), ask questions in natural language, and get context-aware answers using OpenAI's GPT models and semantic search. Features secure user authentication, role-based access, a modern web UI, and a rich public REST API for Q&A.

---

## ✨ Features

- **Document Upload**: TXT, PDF, and DOCX file support (auto text extraction via Apache Tika)
- **AI-Powered Q&A**: Ask questions about your documents, get context-aware answers
- **Semantic Search**: Relevant context extraction using chunking, embeddings, and vector similarity (pgvector)
- **Chunking**: Large documents are split into ~200-word chunks for scalable semantic search
- **Prompt Engineering**: System prompt, context markers, and few-shot examples to prevent hallucinations
- **User Accounts**: Register, login, and manage your own question/answer history
- **Role-Based Security**: JWT authentication, user/ADMIN roles, secure endpoints
- **Edit/Delete Q&A**: Only owners or admins can edit/delete questions
- **Public Q&A API**: Search and access questions/answers by ID, text, or topic (no auth required)
- **Web UI**: Ask questions and view your history via a modern Thymeleaf interface
- **REST API**: Clean, documented endpoints for all operations (Swagger/OpenAPI)

---

## 🛠️ Technology Stack

| Component         | Technology                |
|-------------------|--------------------------|
| Backend           | Spring Boot 3.5.3        |
| AI Integration    | Spring AI, OpenAI API    |
| Vector DB         | pgvector + PostgreSQL    |
| Persistence       | Spring Data JPA          |
| Security          | Spring Security, JWT     |
| Web UI            | Thymeleaf                |
| Build Tool        | Maven                    |
| Language          | Java 21                  |
| Text Extraction   | Apache Tika              |

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.6+
- PostgreSQL 12+ with pgvector extension
- OpenAI API key

### 1. Database Setup

```sql
CREATE DATABASE document_analyser;
\c document_analyser
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Configuration

Set your OpenAI API key:
```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/document_analyser
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Visit: [http://localhost:8080/ask](http://localhost:8080/ask) (web UI)

---

## 🔐 Authentication & Security

- **Register:** `POST /auth/register` (JSON: username, password, [role])
- **Login:** `POST /auth/login` (JSON: username, password) → returns JWT
- **JWT Required:** All API and UI endpoints except `/auth/*` and `/public/*`
- **Role-based:** Default role is USER; ADMIN role for management
- **Edit/Delete:** Only owners or admins can edit/delete questions

**Example: Register & Login**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"username":"alice","password":"pass123"}' http://localhost:8080/auth/register
curl -X POST -H "Content-Type: application/json" -d '{"username":"alice","password":"pass123"}' http://localhost:8080/auth/login
# Use the returned token as: Authorization: Bearer <token>
```

---

## 📡 API Endpoints

### Document Management

- **Upload Document:**  
  `POST /api/documents` (multipart/form-data, param: `file`)

### Q&A (Authenticated)

- **Ask Question:**  
  `POST /api/questions` (JSON: text, documentId)  
  Returns: `{ "answer": "...", "generatedAt": "..." }`

- **Question History:**
  `GET /api/questions/history?documentId={id}&order=asc|desc&page={n}`
  Returns: your question history paged and sorted by date (ascending by default). Use `order=desc` for newest first and optionally filter by `documentId`.

- **Edit Question:**  
  `PUT /api/questions/{id}` (JSON: newText)  
  Only owner or admin can edit

- **Delete Question:**  
  `DELETE /api/questions/{id}`  
  Only owner or admin can delete

### Public Q&A API (No Auth Required)

- **Get by ID:**  
  `GET /public/question/{id}`
- **Search by Text:**  
  `GET /public/search?query=...`
- **Get by Topic:**  
  `GET /public/topic/{topic}`

All public endpoints return:
```json
{
  "id": 1,
  "question": "...",
  "answer": "...",
  "askedAt": "...",
  "answeredAt": "...",
  "topic": "...",
  "type": "pdf|docx|txt"
}
```

### User Interface (Thymeleaf)

- **Ask a Question:**  
  `GET /ask` (form), `POST /ask` (submit question, see answer)
- **View History:**
  `GET /history` (table of your questions/answers with filter, sort and paging controls)

---

## 🖥️ Web UI

- **/ask**: Submit a question about a document (enter question and document ID)
- **/history**: View your own question/answer history (table, sortable)

---

## 🏗️ Project Structure

```
src/main/java/com/example/Document_analiser/
├── DocumentAnaliserApplication.java    # Main application class
├── entity/
│   ├── User.java, Role.java           # User authentication & roles
│   ├── Document.java                  # Document entity
│   ├── Question.java                  # Question entity (linked to User & Document)
│   └── Answer.java                    # Answer entity
├── repository/
│   ├── UserRepository.java
│   ├── DocumentRepository.java
│   ├── QuestionRepository.java
│   └── AnswerRepository.java
├── service/
│   ├── QuestionService.java
│   ├── DocumentService.java
│   ├── AuthService.java               # Role/ownership checks
│   └── CustomUserDetailsService.java
├── controller/
│   ├── AuthController.java            # Register/login endpoints
│   ├── UiController.java              # Thymeleaf UI endpoints
│   ├── QuestionController.java        # REST API for questions (edit/delete)
│   ├── PublicQuestionController.java  # Public Q&A API
│   └── DocumentController.java        # REST API for documents
├── security/
│   ├── JwtUtil.java, JwtFilter.java   # JWT authentication
├── dto/
│   ├── RegisterRequest.java, LoginRequest.java, JwtResponse.java
│   ├── QuestionRequest.java, AnswerResponse.java, QuestionHistoryDto.java, QuestionAnswerDto.java, QuestionUpdateRequest.java
├── config/
│   ├── SecurityConfig.java, DatabaseConfig.java
└── resources/templates/
    ├── ask.html, history.html         # Thymeleaf UI templates
```

---

## 📊 Data Model

- **User**: id, username, password (hashed), role (USER/ADMIN)
- **Document**: id, name, type, uploadDate, content
- **Question**: id, text, askedAt, topic, document (ref), user (ref), answer (ref)
- **Answer**: id, text, generatedAt, question (ref)

---

## 📝 Example Usage

**Upload a document:**
```bash
curl -X POST -F "file=@document.pdf" http://localhost:8080/api/documents -H "Authorization: Bearer <token>"
```

**Ask a question (API):**
```bash
curl -X POST -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"text":"What is this document about?","documentId":1}' \
  http://localhost:8080/api/questions
```

**Edit a question:**
```bash
curl -X PUT -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"newText":"Updated question text"}' \
  http://localhost:8080/api/questions/1
```

**Delete a question:**
```bash
curl -X DELETE -H "Authorization: Bearer <token>" http://localhost:8080/api/questions/1
```

**Public Q&A search:**
```bash
curl http://localhost:8080/public/search?query=security
```

**Web UI:**
- Go to `/ask` to submit a question
- Go to `/history` to view your own Q&A history

---

## 🆘 Support

- See [SETUP.md](SETUP.md) for detailed setup
- For issues, open a GitHub issue or check logs

---

**Built with ❤️ using Spring Boot, OpenAI, Apache Tika, and Thymeleaf**