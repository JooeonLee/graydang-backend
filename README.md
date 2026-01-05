## 🏷️ 서비스명
의안 조회 및 진행 상태 알림 서비스
, 그레이픽(Gray-Pick)

## 📝 서비스 소개
<img width="1920" height="1080" alt="Image" src="https://github.com/user-attachments/assets/545be29b-ac15-4363-baae-4f2eebe5a7b8" />

> **그레이픽(gray-pick)은 여러분이 쉽고 빠르게 관심 의안을 확인하고 팔로우할 수 있는 의안 알림 서비스입니다.**
<img width="1920" height="1080" alt="Image" src="https://github.com/user-attachments/assets/b447a85c-215c-4c4d-a401-6b083db70e33" />

## 🏗️ System Architecture
<img width="1031" height="741" alt="Image" src="https://github.com/user-attachments/assets/f4eaf825-2c47-438e-8cd6-114855972185" />

## 🗄️ ERD
<img width="3190" height="1712" alt="Image" src="https://github.com/user-attachments/assets/f0443881-97af-4e32-8352-e28ebfd27434" />

## 🛠️ Tech Stack
### Backend
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-%6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-%6DB33F?logo=springsecurity&logoColor=white)
![Spring JPA](https://img.shields.io/badge/Spring%20JPA-%6DB33F?logo=&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch-%6DB33F?logo=&logoColor=white) <br>
![Amazon EC2](https://img.shields.io/badge/Amazon%20EC2-FF9900?logo=amazonec2&logoColor=white)
![Amazon RDS](https://img.shields.io/badge/Amazon%20RDS-527FFF?logo=amazonRDS&logoColor=white)
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FC390E?logo=amazons3&logoColor=white) <br>
![Github Actions](https://img.shields.io/badge/Github%20Actions-2088FF?logo=githubactions&logoColor=white) ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=white) <br>
![QueryDSL](https://img.shields.io/badge/QueryDSL-00465B?logo=&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-512BD4?logo=&logoColor=white) <br>

### Co-Work Tool
![Jira](https://img.shields.io/badge/Jira-0052CC?logo=jira&logoColor=white)
![Github](https://img.shields.io/badge/Github-181717?logo=github&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?logo=slack&logoColor=white)
![figma](https://img.shields.io/badge/Figma-F24E1E?logo=figma&logoColor=white)
![notion](https://img.shields.io/badge/Notion-000000?logo=notion&logoColor=white)

## 💡 기술 스택 선정 이유
- **Spring Boot**
  - 스프링 프레임워크를 기반으로 자동 설정과 스타터 종속성을 제공하여 웹 애플리케이션 개발과 배포를 신속하게 수행할 수 있도록 돕습니다.

- **JPA (Java Persistence API)**
  - SQL 작성 없이 객체 지향적 방식으로 데이터베이스와 상호작용할 수 있게 해주는 자바의 표준 ORM 기술입니다.

- **Querydsl**
  - 타입 안전을 보장하는 프레임워크를 통해 복잡한 쿼리를 쉽게 작성하고 컴파일 시점에서 오류를 잡을 수 있습니다.

- **AWS RDS (MySQL)**
  - 서버 유지보수나 패치 관리 없이 MySQL 데이터베이스를 운영할 수 있는 AWS의 관리형 서비스입니다.

- **AWS S3 Storage**
  - 높은 가용성과 보안을 제공하는 객체 스토리지 서비스로, 어떠한 양의 데이터도 저장하고 검색할 수 있습니다.

- **Java 17**
  - 장기적인 안정성과 보안 업데이트를 제공하는 최신 Long-Term Support (LTS) 버전의 자바입니다.

- **Spring Batch**
  - 대용량 데이터 처리를 위한 배치 처리 프레임워크로, 스케줄링, 트랜잭션 관리, 재시도, 로깅 등을 지원합니다.
  - **Scheduler** (Spring Scheduler) 와 연동하여 주기적으로 의안 정보를 갱신하거나 신규 의안에 대한 GPT 요약을 생성 할 때 사용합니다.

- **Spring Security**
  - 스프링 기반 애플리케이션의 보안을 강화하며, 인증과 권한 부여, CSRF 방어 등을 포함한 종합적인 보안 솔루션을 제공합니다.

- **JWT (JSON Web Tokens)**
  - 서버와 클라이언트 간의 정보를 토큰 형태로 간소화하며, 상태를 저장하지 않는 서비스에 적합한 인증 시스템입니다.

- **GitHub Actions**
  - 코드 통합, 테스트, 배포를 자동화하여 개발 워크플로우의 효율성을 향상시키는 CI/CD 도구입니다.

- **Swagger**
  - RESTful API의 설계, 빌드, 문서화를 지원하며 프론트와 백엔드 개발자 간의 협업과 API 이해를 증진시키는 소프트웨어 프레임워크입니다.

- **Spring Scheduler**
  - 스프링 프레임워크 내에서 주기적인 작업을 쉽게 설정하고 관리할 수 있게 해주는 스케줄링 기능입니다.

- **Jsoup**
  - HTML에서 데이터를 파싱하고 조작하는 자바 라이브러리로, 정적 웹 크롤링 및 데이터 추출 작업에 유용합니다.

## 📌 Convention
### Naming Convention
- 파일 : CamelCase + SnakeCase
- 클래스명 : PascalCase
- 함수/변수명 : CamelCase

### Branch Naming Convention
- main
- develop
- feature/
- hotfix/
- refactor/

### Commit Convention

| Tag      | Description                                         |
|----------|-----------------------------------------------------|
| `feat`   | Commits that add a new feature.                     |
| `fix`    | Commits that fix a bug.                             |
| `hotfix` | Fix an urgent bug in issue or QA.                   |
| `build`  | Commits that affect build components.               |
| `chore`  | Miscellaneous commits.                              |
| `style`  | Commits for code styling or format.                 |
| `docs`   | Commits that affect documentation only.             |
| `test`   | Commits that add missing tests or correcting existing tests. |
| `refactor`| Commits for code refactoring.                      |

### Coding Convention
> 코드를 작성한 의도와 목적을 명확하게 드러냅니다.
- **Variables** : 축약어를 사용하지 않습니다. 변수 명에 자료구조를 포함하지 않습니다. (e.g., `CarList.java` ❌) <br>
- **Methods**: `lowerCamelCase`를 사용합니다. 메소드 명은 동사 및 전치사로 시작합니다. (e.g., `getUserInfo()`) <br>
- **Database Columns**: `snake_case`를 사용합니다. (e.g., `member_id`) <br>
- **End Points**: REST API를 준수합니다. (e.g., `GET` www.example.com/users/1)

## 🌿 Branch Flow
- GitHub Flow 기반으로, `develop` 통합 브랜치를 둔 변형(variant)으로 운영
- 초기 개발 단계에서는 `develop`을 중심으로 기능 개발(feature/*, refactor/*) → PR merge 방식으로 운영
릴리즈(배포) 시점부터 `main`을 추가하여 배포 브랜치로 고정
- 릴리즈 절차: `develop`에서 안정화 → `main`에 merge(또는 tag) → 배포
