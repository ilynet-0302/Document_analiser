# 📄 Document Analyzer

A full-stack Spring Boot application for intelligent document analysis. Upload documents, ask questions in natural language, and get context-aware answers using OpenAI's GPT models and semantic search. Features secure user authentication, role-based access, and a modern web UI with Thymeleaf.

---

## ✨ Features

- **Document Upload**: TXT file support (PDF/DOCX coming soon)
- **AI-Powered Q&A**: Ask questions about your documents, get context-aware answers
- **Semantic Search**: Relevant context extraction using vector similarity (pgvector)
- **User Accounts**: Register, login, and manage your own question/answer history
- **Role-Based Security**: JWT authentication, user/ADMIN roles, secure endpoints
- **Web UI**: Ask questions and view your history via a modern Thymeleaf interface
- **REST API**: Clean, documented endpoints for all operations

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
- **JWT Required:** All API and UI endpoints except `/auth/*`
- **Role-based:** Default role is USER; ADMIN role for future features

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

- **Ask Question (API):**  
  `POST /api/questions` (JSON: text, documentId)  
  Returns: `{ "answer": "...", "generatedAt": "..." }`

- **Question History (API):**  
  `GET /api/questions/history`  
  Returns: list of your questions/answers

### User Interface (Thymeleaf)

- **Ask a Question:**  
  `GET /ask` (form), `POST /ask` (submit question, see answer)

- **View History:**  
  `GET /history` (table of your questions/answers)

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
│   └── CustomUserDetailsService.java
├── controller/
│   ├── AuthController.java            # Register/login endpoints
│   ├── UiController.java              # Thymeleaf UI endpoints
│   ├── QuestionController.java        # REST API for questions
│   └── DocumentController.java        # REST API for documents
├── security/
│   ├── JwtUtil.java, JwtFilter.java   # JWT authentication
├── dto/
│   ├── RegisterRequest.java, LoginRequest.java, JwtResponse.java
│   ├── QuestionRequest.java, AnswerResponse.java, QuestionHistoryDto.java
├── config/
│   ├── SecurityConfig.java, DatabaseConfig.java
└── resources/templates/
    ├── ask.html, history.html         # Thymeleaf UI templates
```

---

## 📊 Data Model

- **User**: id, username, password (hashed), role (USER/ADMIN)
- **Document**: id, name, type, uploadDate, content
- **Question**: id, text, askedAt, document (ref), user (ref), answer (ref)
- **Answer**: id, text, generatedAt, question (ref)

---

## 📝 Example Usage

**Upload a document:**
```bash
curl -X POST -F "file=@document.txt" http://localhost:8080/api/documents -H "Authorization: Bearer <token>"
```

**Ask a question (API):**
```bash
curl -X POST -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"text":"What is this document about?","documentId":1}' \
  http://localhost:8080/api/questions
```

**Web UI:**
- Go to `/ask` to submit a question
- Go to `/history` to view your own Q&A history

---

## 🏁 Roadmap

- [x] User authentication & JWT security
- [x] Per-user question/answer history
- [x] Web UI with Thymeleaf
- [x] REST API for all operations
- [ ] PDF/DOCX support
- [ ] Vector search for semantic context
- [ ] Admin features & advanced analytics

---

## 🆘 Support

- See [SETUP.md](SETUP.md) for detailed setup
- For issues, open a GitHub issue or check logs

---

**Built with ❤️ using Spring Boot, OpenAI, and Thymeleaf**