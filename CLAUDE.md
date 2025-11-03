# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Graypick (내부명: Graydang App) is a Spring Boot backend application that provides a REST API for tracking and engaging with Korean legislative bills. The application integrates with the Korean National Assembly API and uses GPT for bill summarization.

## Essential Commands

### Build and Run
```bash
# Clean build
./gradlew clean build

# Build without tests
./gradlew build -x test

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.graydang.app.domain.bill.service.BillServiceTest"

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Docker Commands
```bash
# Start all services (app, redis, nginx, monitoring)
docker-compose up -d

# View logs
docker-compose logs -f app

# Rebuild and restart app container
docker-compose build app && docker-compose up -d app
```

## Code Architecture

### Domain-Driven Design Structure
The codebase follows DDD principles with clear domain boundaries under `src/main/java/com/graydang/app/domain/`:

- **auth**: OAuth2/JWT authentication with Google/Kakao providers
- **bill**: Core domain for legislative bills with batch processing integration
- **user**: User profile management and interactions (likes, bookmarks)
- **comment**: Commenting system with nested replies and likes
- **admin**: Administrative functions for content moderation
- **search**: Search functionality across bills and content

### Key Architectural Patterns

1. **Controller → Service → Repository** layering with clear separation
2. **Global exception handling** via `@RestControllerAdvice` in `global/common/error/`
3. **AOP-based logging** for all controller methods via `LoggingAspect`
4. **JWT authentication** with refresh tokens stored in Redis
5. **Batch processing** for bill data synchronization from external APIs

### Critical Integration Points

1. **External APIs**:
   - Korean National Assembly Bill API (batch/job/)
   - OpenAI GPT API for bill summarization (batch/gpt/)

2. **AWS S3**: File uploads handled by `S3Service` with presigned URLs

3. **Redis**: Used for refresh token storage and caching

4. **Spring Batch**: Multiple job configurations for:
   - Bill data fetching and processing
   - GPT summarization
   - Scheduled via `BillScheduler`

### Environment Configuration

The application uses profile-based configuration:
- `application.yml`: Base configuration
- `application-local.yml`: Local development
- `application-prod.yml`: Production settings
- `application-oauth.yml`: OAuth2 credentials
- `application-monitoring.yml`: Actuator/metrics endpoints

Key environment variables needed:
- `JWT_SECRET_KEY`: JWT signing key
- `SLACK_BOT_TOKEN`: For Slack notifications
- `OPENAI_API_KEY`: For GPT integration
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`: S3 access
- Database credentials and OAuth client secrets

### API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

All API endpoints follow RESTful conventions with consistent response formats using `ResponseEntity<ApiResponse<T>>`.

### Testing Approach

Currently minimal test coverage. When adding tests:
- Use `@SpringBootTest` for integration tests
- Use `@ActiveProfiles("test")` to use test configuration
- H2 in-memory database is configured for testing
- Mock external services (S3, APIs) in tests

### Development Notes

1. **QueryDSL**: Used extensively for complex queries. Q-classes are generated during build.

2. **Security**: All endpoints except auth/public require JWT authentication. Use `@PreAuthorize` for method-level security.

3. **Monitoring**: Actuator endpoints exposed at `/actuator/*` with Prometheus metrics at `/actuator/prometheus`.

4. **Batch Jobs**: Can be triggered manually via REST endpoints in development mode.

5. **Entity Auditing**: Base entities use JPA auditing for createdAt/updatedAt fields automatically.

### Documentation Guidelines

When creating execution plans or architectural documentation for tasks:
- Create documentation files in the `docs/` directory
- Use descriptive filenames that indicate the content (e.g., `docs/feature-implementation-plan.md`, `docs/api-design.md`)
- Include the creation date and purpose at the top of each document
- Structure documents with clear sections and markdown formatting

# 테스트 코드 작성 규칙

## 0. 테스트 코드 작성 전 준비
- 컨트롤러 통합테스트의 경우 지시가 없는한 작성하지 말아주세요
- service 통합테스트, repository, domain 객체에 대한 단위 테스트만 실행해주세요

## 1. 테스트 클래스 구조

- 테스트 클래스는 테스트 대상 클래스와 동일한 패키지에 위치
- 테스트 클래스 이름은 테스트 대상 클래스 이름 + `Test`로 명명
- 관련된 테스트들은 `@Nested` 클래스로 그룹화하여 가독성을 높인다.
- 네이밍 컨벤션을 추가하여 테스트 코드의 명확성과 일관성을 높입니다.

```java
public class EmployeeSchedulesTest {
  @Nested
  public class FindCurrentWeekSchedulesTest {
    // 관련 테스트 메서드들
  }
}
```

## 2. 테스트 메서드 명명 규칙

- 테스트 메서드 이름은 `테스트대상메서드_테스트시나리오` 형식으로 작성
- 테스트 메서드 이름은 `테스트대상메서드_테스트시나리오` 형식으로 작성한다.
- 카멜 케이스를 사용하여 가독성을 높인다.
- `test` 접두사는 사용하지 않는다.
- 테스트 변수는 카멜 케이스 사용 (예: `testDate`, `testStartTime`)
- 상수만 `static final` 키워드와 함께 대문자 상수명 사용 (예: `private static final String TEST_CONSTANT = "value";`)

```java
public void findCurrentWeekSchedules_shouldReturnOnlyWeekSchedules() { ... }
public void findCurrentWeekSchedules_shouldFilterNullDates() { ... }
public void findCurrentWeekSchedules_shouldReturnEmptyListWhenNoSchedules() { ... }
```

## 3. DisplayName 사용

- `@DisplayName` 어노테이션을 사용하여 테스트의 목적을 한글로 명확하게 표현한다.
- `@Nested` 클래스에도 `@DisplayName`을 사용하여 테스트 그룹의 목적을 설명한다.
- 클래스 레벨의 `@DisplayName`은 필요한 경우에만 사용한다.

```java
@Nested
@DisplayName("주간 스케줄 조회 시 주간 스케줄만 반환하고 null 날짜는 필터링한다")
public class FindCurrentWeekSchedulesTest {

  @Test
  @DisplayName("주간 스케줄을 정확히 반환한다")
  public void findCurrentWeekSchedules_shouldReturnOnlyWeekSchedules() { ... }
}
```

### 테스트 데이터 설정
- 테스트에서는 `LocalDate.now()`와 같은 동적 값 대신 고정된 날짜 사용
- 테스트 데이터의 의미를 주석으로 명확히 표현
- Use `List.of()` and `Map.of()` for immutable collections
- Only pass necessary values to test object builders
- Add comments to clarify test data meaning

```java
// 2025-12-25 (목요일)
LocalDate specificDate = LocalDate.of(2025, 12, 25);
// 2025-12-22 (월요일)
LocalDate startOfWeek = LocalDate.of(2025, 12, 22);
// 2025-12-28 (일요일)
LocalDate endOfWeek = LocalDate.of(2025, 12, 28);
```

## 5. 테스트 구조

- 테스트 코드는 준비, 실행, 검증 구조를 따르되, 간단한 유닛 테스트에서는 주석(given, when, then 또는 Arrange, Act, Assert)을 사용하지 않는다.
- 테스트 코드만으로 의도가 명확히 드러나도록 작성한다.
- 복잡한 테스트에서만 필요에 따라 구조를 명시적으로 구분한다.

```java
// 간단한 유닛 테스트 - 주석 없이 작성
EmployeeScheduleDateRange dateRange = new EmployeeScheduleDateRange(startOfWeek, endOfWeek);
EmployeeSchedules employeeSchedules = EmployeeSchedules.builder()
    .schedules(List.of(schedule1, schedule2))
    .build();

List<LocalDate> result = employeeSchedules.findCurrentWeekSchedules(dateRange);

assertThat(result).containsExactly(startOfWeek);
```

## 6. Assertions 사용

- AssertJ의 `assertThat()`을 사용하여 가독성 높은 검증문을 작성한다.
- 복잡한 객체 검증 시 `extracting()`을 적극 활용하여 코드를 간결하게 유지한다.

```java
// extracting 사용 예시
assertThat(result)
    .extracting("employeeSeq", "opTimeCode", "enableYn")
    .containsExactly(123, 456, "Y");
```

## 7. 테스트 독립성

- 각 테스트는 독립적으로 실행 가능해야 한다.
- 테스트 간 의존성이 없어야 한다.
- 테스트 실행 순서에 의존하지 않아야 한다.


### Assertion Best Practices
- Use AssertJ for all assertions
- Use `tuple` for verifying multiple values:
  ```java
  assertThat(orders)
      .extracting("orderNumber", "totalAmount", "status")
      .containsExactly(
          tuple("ORD001", 50000, OrderStatus.CONFIRMED),
          tuple("ORD002", 30000, OrderStatus.PENDING)
      );
  ```

### Testing
- Integration tests extend base test classes
- QueryDSL repositories have dedicated test classes
- Use H2 for test database (configured automatically)
- Test fixtures in `TestEntityFactory.java`
- @MockitoBean으로 의존성 모킹: 서비스 레이어와 외부 의존성을 모킹

#### Test Result Visibility Guidelines
- **Console Output Required**: All test methods must include console output to show test execution results
- **Success Pattern**: Use `System.out.println("✅ [testMethodName] 테스트 통과 - 설명: " + result.size() + "건");`
- **Descriptive Messages**: Include meaningful descriptions about what was tested and result counts
- **Example Implementation**:
```java
@Test
void findAll() {
    List<PatientSimpleResponse> all = patientQueryDslRepository.findAll(patientSearch, pageable);
    assertThat(all).isNotNull();
    System.out.println("✅ [findAll] 테스트 통과 - 전체 환자 조회: " + all.size() + "건");
    assertThat(all.size()).isGreaterThanOrEqualTo(0);
}
```