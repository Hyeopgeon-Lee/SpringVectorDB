# SpringVectorDB — Spring Boot 4 / MongoDB Vector Search 실습

**Java 17 · Spring Boot 4.1.1 · Spring Cloud 2025.1.3 · MongoDB** 학습 프로젝트입니다.

기존과 동일하게 외부 임베딩 HTTP API(`POST /embedding`)로 벡터를 생성하고,
MongoDB의 `documents` 컬렉션에 **텍스트와 임베딩 값**을 저장합니다.
질문을 임베딩한 뒤 MongoDB 문서를 읽어 **메모리에서 코사인 유사도**를 계산해
상위 10개를 반환합니다.

> **중요:** 이 프로젝트는 **Spring Data MongoDB + OpenFeign** 구현입니다.
> Spring Data JPA, PGVector, MongoDB Atlas Vector Search 및 Spring AI VectorStore는 사용하지 않습니다.
> `findAll()`을 사용하는 예제이므로 문서 수가 많아지면 MongoDB Vector Search 인덱스로
> 대체해야 합니다. 이 README는 현재 구현에 맞게 정정했습니다.

## 요구 사항

- JDK 17
- Gradle 8.14.3 (Wrapper 포함)
- MongoDB 서버 (기본: 로컬 27017)
- 임베딩 서버 (기본: `http://localhost:12000/embedding`)
- 서버가 반환하는 JSON: `{"embedding":[0.1,0.2,0.3]}` 등 **유한한 숫자 벡터**

## 주요 의존성

- Spring Boot 4.1.1
- Spring Cloud OpenFeign (Spring Cloud BOM 2025.1.3)
- Spring Data MongoDB
- Spring MVC, Jackson 3
- Lombok

## 환경변수 설정

`src/main/resources/application.yaml`:

```yaml
server:
  port: ${PORT:11000}

spring:
  mongodb:
    uri: ${MONGODB_URI:mongodb://localhost:27017/myDB}

embedding:
  api:
    url: ${EMBEDDING_API_URL:http://localhost:12000}
```

예시 (Linux/macOS):

```bash
export MONGODB_URI='mongodb://localhost:27017/myDB'
export EMBEDDING_API_URL='http://localhost:12000'
```

인증이 설정된 MongoDB라면 `MONGODB_URI` 환경변수에 해당 연결 정보를 지정하세요.
**접속 비밀번호를 Git 또는 application.yaml에 커밋하지 마세요.**
기존 코드의 MongoDB 비밀번호를 실제 사용했다면 반드시 교체하세요. Git 이력에 값이 남을 수 있습니다.

## 빌드, 테스트, 실행

```bash
bash ./gradlew clean test bootJar
bash ./gradlew bootRun
```

Windows:

```powershell
gradlew.bat clean test bootJar
gradlew.bat bootRun
```

GitHub Actions는 JDK 17 환경에서 빌드와 자동 회귀 테스트를 실행합니다.
단위 테스트는 MongoDB/Embedding API의 가짜 객체를 사용하며 실서버가 필요하지 않습니다.

## API (기존 URL 유지)

| 메서드 | 경로 | 요청 형식 |
|---|---|---|
| POST | `/vector/v1/document` | 본문에 원시 텍스트 |
| POST | `/vector/v1/batch-documents` | JSON `{"contents":["내용1","내용2"]}` |
| POST | `/vector/v1/generate` | 본문에 검색 질문 텍스트 |

```bash
# 단일 문서 저장 (content-type: text/plain)
curl -X POST http://localhost:11000/vector/v1/document \
  -H 'Content-Type: text/plain; charset=UTF-8' \
  --data-binary 'Spring Boot 4와 MongoDB 실습'

# 여러 문서 저장
curl -X POST http://localhost:11000/vector/v1/batch-documents \
  -H 'Content-Type: application/json' \
  -d '{"contents":["첫 번째 문서","두 번째 문서"]}'

# 코사인 유사도 검색
curl -X POST http://localhost:11000/vector/v1/generate \
  -H 'Content-Type: text/plain; charset=UTF-8' \
  --data-binary 'Spring Boot 4란 무엇인가요?'
```

공통 응답의 `data`에 저장 결과 또는 일치 문서 배열을 반환합니다.
검색 결과는 코사인 유사도 0.5 이상인 문서 중 최대 10개입니다.

## Spring Boot 4 마이그레이션

- `spring-boot-starter-webmvc`, `spring-boot-starter-data-mongodb` 사용
- Boot 4 설정 키 `spring.mongodb.uri` 사용 (`spring.data.mongodb` 접속 정보 설정 대체)
- Spring Cloud OpenFeign BOM을 Boot 4 호환 릴리스로 변경
- 임베딩 벡터의 숫자 유효성, 차원 불일치, 0 벡터 처리
- 잘못된 문서 본문/빈 문서 목록에 HTTP 400 반환
- Feign에서 전체 요청/응답 본문 기록하지 않도록 BASIC 로그로 변경
- Gradle Wrapper 업데이트 및 CI 테스트 추가

## 운영 주의사항

이 프로젝트는 학습용으로 **인증/인가가 구현되어 있지 않습니다.**
공용 인터넷에 그대로 배포하지 말고 내부 실습망에서 사용하세요.
운영 환경에서는 인증, 권한 확인, TLS, 요청 크기 제한, 속도 제한,
MongoDB 벡터 인덱스 또는 전용 VectorStore를 도입해야 합니다.

**검증 범위:** Gradle 컴파일·단위 테스트·실행 JAR 생성은 GitHub Actions로 확인합니다.
MongoDB/외부 임베딩 서버와의 실제 통합 실행은 해당 서버 환경에서 따로 검증해야 합니다.
