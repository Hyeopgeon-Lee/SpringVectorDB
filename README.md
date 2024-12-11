SpringVectorDB

SpringVectorDB는 MongoDB를 벡터 데이터베이스로 활용하여 유사도 검색 기능을 제공하는 Spring Boot 기반 프로젝트입니다.

주요 기능

벡터 데이터 저장 및 관리
MongoDB에 벡터 데이터를 효율적으로 저장하고 관리합니다.

유사도 검색
입력된 벡터와 저장된 벡터 데이터 간의 유사도를 계산하여 가장 유사한 항목을 빠르게 검색합니다.

RESTful API 제공
유사도 검색 및 벡터 데이터 관리 기능을 RESTful API 형태로 제공합니다.


사용된 기술 스택

Java 17

Spring Boot: 애플리케이션 프레임워크

Spring Data MongoDB: MongoDB와의 데이터 접근을 위한 라이브러리

MongoDB: NoSQL 데이터베이스

Gradle: 빌드 도구


설치 및 실행 방법

1. 프로젝트 클론

git clone https://github.com/Hyeopgeon-Lee/SpringVectorDB.git
cd SpringVectorDB

2. MongoDB 설정

MongoDB를 설치하고 실행합니다. 기본 설정으로 localhost:27017에 MongoDB가 실행되어 있어야 합니다.

3. 의존성 설치 및 빌드

./gradlew build

4. 애플리케이션 실행

./gradlew bootRun

API 사용법

1. 벡터 데이터 저장

POST /api/vectors

{
  "id": "unique-id",
  "vector": [0.1, 0.2, 0.3, 0.4]
}

2. 유사도 검색

POST /api/vectors/similarity

{
  "vector": [0.1, 0.2, 0.3, 0.4]
}

응답 예시

[
  {
    "id": "unique-id-1",
    "similarityScore": 0.95
  },
  {
    "id": "unique-id-2",
    "similarityScore": 0.92
  }
]

프로젝트 구조

SpringVectorDB/
├── src/
│   ├── main/
│   │   ├── java/com/example/springvectordb/
│   │   │   ├── SpringVectorDbApplication.java
│   │   │   ├── controller/
│   │   │   │   └── VectorController.java
│   │   │   ├── model/
│   │   │   │   └── VectorData.java
│   │   │   └── repository/
│   │   │       └── VectorRepository.java
│   │   └── resources/
│   │       └── application.yml
└── build.gradle

기여 방법

1. 이슈를 생성하여 버그 또는 개선 사항을 알려주세요.


2. 포크한 후 브랜치를 생성합니다.

git checkout -b feature/새로운기능


3. 변경 사항을 커밋하고 푸시합니다.

git commit -m "Add 새로운 기능"
git push origin feature/새로운기능


4. Pull Request를 제출해 주세요.



라이선스

이 프로젝트는 MIT License에 따라 배포됩니다. 자세한 내용은 LICENSE 파일을 참고하세요.


---

이 README는 프로젝트를 이해하고 실행하는 데 필요한 핵심 내용을 포함하고 있습니다. 필요에 따라 추가하거나 수정할 수 있습니다.

