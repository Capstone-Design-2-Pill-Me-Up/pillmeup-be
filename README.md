# 💊 Pill Me Up Backend
Vision AI 기반 의약품 식별 서비스 Pill Me Up의 백엔드 레포지토리

<br></br>

## 📘 프로젝트 개요
스마트폰 카메라로 촬영한 알약 이미지를 AI가 분석하여  
의약품명, 성분, 용법 및 9가지 DUR 주의사항(병용금기, 임부금기 등)을 안내하는 서비스입니다.

Spring Boot 기반 REST API 서버로,
AI 모델 서버, 식약처 DUR·e약은요 API, MySQL 데이터베이스와 연동됩니다.

<br></br>

## 🌐 API 문서
Swagger UI:  
👉 [https://pillmeup.site/swagger-ui/index.html](https://wonsandbox.cloud/swagger-ui/index.html)

<br></br>

## 🧱 Technology Stack

| Category | Technology |
|-----------|-------------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.9 |
| ORM / Persistence | Spring Data JPA |
| Database | MySQL |
| Authentication | JWT (Spring Security) |
| Documentation | SpringDoc Swagger UI |
| Infrastructure | AWS EC2 / RDS / S3 |
| Build Tools | Gradle |
| AI Integration | Vision AI Server (YOLOv8) |
| External API | 식약처 DUR API, e약은요 API |

<br></br>

## 🔨 Architecture


<br></br>

## ⚙️ 주요 기능

| 기능 | 설명 |
|------|------|
| 회원가입 / 로그인 (JWT) | 사용자 인증 및 토큰 기반 인가 처리 |
| 알약 이미지 업로드 | 업로드된 이미지 → Vision AI 서버로 전송 후 분석 결과 수신 |
| 의약품 정보 조회 | 식약처 DUR + e약은요 API 기반 품목 정보 조회 |
| 주의사항 안내 | 9가지 주의사항(임부금기, 병용금기 등) 분류 및 GPT 보완 설명 |
| 사용 이력 관리 | 사용자가 조회한 의약품 및 분석 결과 자동 저장 |
| Swagger 문서화 | REST API 명세 자동 생성 |

<br></br>

## 🧾 Commit Convention
`type: 작업 내용`

| Type | Description |
|------|--------------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 |
| `docs` | 문서 추가/수정 |
| `test` | 테스트 코드 추가 |
| `chore` | 설정, 빌드, 기타 작업 |

<br></br>

## 🌿 Branch Strategy
- 브랜치 네이밍: `컨벤션명/작업내용`  
- 모든 작업은 feature → develop → main 순으로 병합  
- 긴급 수정 시: `!hotfix:` 사용

<br></br>

## 🚀 배포 환경
| 항목 | 내용 |
|------|------|
| Server | AWS EC2 |
| Database | AWS RDS (MySQL 8.0) |
| Storage | AWS S3 (이미지 저장) |
| CI/CD | GitHub Actions + EC2 배포 자동화 |
| Port | 8080 (백엔드), 80/443 (Nginx Reverse Proxy) |

<br></br>

## 🧠 기술 선정 이유
- Spring Boot: RESTful API 구현에 최적화된 프레임워크  
- JPA + MySQL: 객체지향적 ORM 기반 데이터 관리  
- AWS 인프라: 안정적인 서비스 운영 및 확장성 확보  
- Swagger UI: API 문서 자동화 및 협업 효율성 향상  
- JWT 인증: Stateless 환경에서 효율적인 인증 유지  
- Vision AI + GPT: 인공지능 기반 의약품 인식 및 주의사항 자동 생성  

<br></br>

## 🧩 프로젝트 구조
```plaintext
src/
├── main/
│   └── domain/
│       ├── drug/                # 의약품 도메인 (엔티티, 리포지토리, 서비스, 컨트롤러)
│       ├── user/                # 사용자 인증/관리
│       ├── history/             # 사용자 이력 관리
│       ├── photo/               # 이미지 업로드 및 분석 결과 저장
│       └── caution/             # DUR 주의사항 관리
│
└── ├── global/
    ├── config/              # CORS, Swagger, Security 등 글로벌 설정
    ├── exception/           # 전역 예외 처리 및 응답 래퍼 (ApiResponse)
    └── security/            # JWT 인증 및 필터
```

