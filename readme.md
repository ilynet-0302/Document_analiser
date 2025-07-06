# 📄 Document Analyzer

A Spring Boot application that enables intelligent document analysis through AI-powered question answering. Upload documents, ask questions in natural language, and get context-aware answers using OpenAI's GPT models and semantic search with pgvector.

## ✨ Features

### 🔍 **Document Processing**
- **File Upload**: Support for TXT files (PDF/DOCX coming soon)
- **Content Extraction**: Automatic text extraction from uploaded documents
- **Metadata Storage**: Document name, type, upload date, and content storage
- **Vector Embeddings**: Semantic indexing using OpenAI embeddings

### 🤖 **AI-Powered Analysis**
- **Natural Language Questions**: Ask questions about your documents
- **Context-Aware Answers**: AI generates responses based on document content
- **Semantic Search**: Find relevant document sections using vector similarity
- **Conversation History**: Track all questions and answers

### 🗄️ **Data Management**
- **PostgreSQL Database**: Reliable document and conversation storage
- **pgvector Integration**: High-performance vector similarity search
- **JPA Entities**: Structured data model for documents, questions, and answers
- **RESTful API**: Clean, documented endpoints for all operations

## 🛠️ Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Backend Framework** | Spring Boot 3.5.3 | Application foundation |
| **AI Integration** | Spring AI | OpenAI API integration |
| **Vector Database** | pgvector + PostgreSQL | Semantic search storage |
| **Data Persistence** | Spring Data JPA | Database operations |
| **API Layer** | Spring Web | REST endpoints |
| **Build Tool** | Maven | Dependency management |
| **Language** | Java 21 | Core development |

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+ with pgvector extension
- OpenAI API key

### 1. Database Setup
```sql
-- Create database
CREATE DATABASE document_analyser;

-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Configuration
```bash
# Set OpenAI API key
export OPENAI_API_KEY=your_openai_api_key_here
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/document_analyser
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Run the Application
```bash
# Build the project
mvn clean install

# Start the application
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## 📡 API Endpoints

### Document Management

#### Upload Document
```http
POST /api/documents
Content-Type: multipart/form-data

file: [your_document.txt]
```

**Response:**
```json
"Document uploaded."
```

#### Ask Question
```http
POST /ask
Content-Type: text/plain

What is this document about?
```

**Response:**
```json
"Dummy response"
```

### Example Usage

#### Using curl
```bash
# Upload a document
curl -X POST -F "file=@document.txt" http://localhost:8080/api/documents

# Ask a question
curl -X POST -H "Content-Type: text/plain" \
  -d "What are the main topics discussed in the document?" \
  http://localhost:8080/ask
```

#### Using Postman
1. Create a new POST request to `http://localhost:8080/api/documents`
2. Set body to `form-data`
3. Add key `file` with type `File`
4. Select your document and send

## 🏗️ Project Structure

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

## 📊 Data Model

### Document Entity
- `id`: Primary key
- `name`: Original filename
- `type`: File extension
- `uploadDate`: Timestamp of upload
- `content`: Extracted text content (LOB)

### Question Entity
- `id`: Primary key
- `text`: Question content
- `askedAt`: Timestamp
- `document`: Reference to related document

### Answer Entity
- `id`: Primary key
- `text`: AI-generated answer (LOB)
- `generatedAt`: Timestamp
- `question`: One-to-one relationship with question

## 🔮 Roadmap

### Phase 1: Core Features ✅
- [x] Document upload and storage
- [x] Basic text extraction
- [x] JPA entity structure
- [x] REST API endpoints
- [x] Database configuration

### Phase 2: AI Integration 🚧
- [ ] OpenAI embedding generation
- [ ] Vector similarity search
- [ ] Context-aware question answering
- [ ] Document chunking for large files

### Phase 3: Enhanced Features 📋
- [ ] PDF and DOCX support (Apache Tika)
- [ ] Document versioning
- [ ] User authentication
- [ ] Web interface (Thymeleaf)
- [ ] Advanced search filters

### Phase 4: Production Ready 🎯
- [ ] Performance optimization
- [ ] Security hardening
- [ ] Monitoring and logging
- [ ] Docker containerization
- [ ] CI/CD pipeline

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For detailed setup instructions, see [SETUP.md](SETUP.md)

For issues and questions:
- Create an issue in the repository
- Check the setup guide for common problems
- Review the application logs for error details

---

**Built with ❤️ using Spring Boot and OpenAI**