# 🌱 SpringVectorDB

**Spring Boot 3.x · Java 17** 기반의 **벡터 데이터베이스(Vector DB) 학습 프로젝트**입니다.  
OpenFeign을 통해 **Embedding API**를 호출하고,  
Spring Data JPA 기반 Repository를 활용해 **문서 벡터를 저장/검색**하는 예제입니다.  

<p align="left">
  <img alt="java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="spring-boot" src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white">
  <img alt="db" src="https://img.shields.io/badge/Vector-DB-blueviolet">
  <img alt="feign" src="https://img.shields.io/badge/OpenFeign-Client-orange">
  <img alt="build" src="https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white">
</p>

---

## ✨ 주요 기능
- **문서(Document) 관리**
  - `Documents` 엔티티 / `DocumentsDTO`
  - 삽입, 조회, 삭제
- **벡터 Embedding**
  - `IEmbeddingClient` : OpenFeign 기반 외부 Embedding API 호출
  - `IEmbeddingService` : 임베딩 생성/검색 서비스
- **REST API**
  - `VectorController` → 문서 삽입/검색/삭제 엔드포인트
- **Repository**
  - `DocumentRepository` → DB와 연동 (벡터/메타데이터 저장)

---

## 🧱 기술 스택
- **Spring Boot 3.x**
- **Spring Data JPA**
- **OpenFeign**
- **Vector Database (예: PGVector, Pinecone, Weaviate 등)**
- **Gradle**
- **Lombok**

---

## 📁 프로젝트 구조(요약)
```
SpringVectorDB/
├─ src/main/java/kopo/poly/
│  ├─ config/         # OpenFeignConfig
│  ├─ controller/     # VectorController
│  ├─ domain/         # Documents 엔티티
│  ├─ dto/            # DocumentsDTO, MsgDTO
│  ├─ repository/     # DocumentRepository
│  ├─ service/        # IEmbeddingClient, IEmbeddingService (+ impl)
│  └─ SpringVectorDbApplication.java
├─ src/main/resources/
│  ├─ application.yaml
│  ├─ static/
│  └─ templates/
├─ build.gradle, settings.gradle
```

---

## ⚙️ 빠른 시작
### 1) 필수 요건
- **JDK 17**
- **Gradle 8.x+**
- **Vector DB** (예: PostgreSQL + PGVector, 또는 외부 SaaS)

### 2) DB 준비 (Postgres + pgvector 예시)
```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    content TEXT,
    embedding vector(1536)
);
```

### 3) 환경설정
`src/main/resources/application.yaml` 예시:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vectorDB
    username: poly
    password: ********
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.PostgreSQLDialect

openai:
  api:
    url: https://api.openai.com/v1/embeddings
    key: ${OPENAI_API_KEY}
```

### 4) 실행
```bash
./gradlew bootRun
```

---

## 🧪 빠른 점검(Quick Test)
### 문서 삽입
```bash
curl -X POST http://localhost:8080/vector/insert   -H "Content-Type: application/json"   -d '{"content":"이것은 벡터DB 학습 예제입니다"}'
```

### 유사도 검색
```bash
curl -X GET "http://localhost:8080/vector/search?query=예제"
```

### 문서 삭제
```bash
curl -X DELETE http://localhost:8080/vector/{id}
```

---

## 📜 라이선스
- 본 저장소는 **Apache-2.0** 라이선스를 따릅니다.  

---

## 🙋‍♀️ 문의
- **한국폴리텍대학 서울강서캠퍼스 빅데이터소프트웨어과**  
- **이협건 교수** · hglee67@kopo.ac.kr  
- 입학 상담 오픈채팅방: <https://open.kakao.com/o/gEd0JIad>
