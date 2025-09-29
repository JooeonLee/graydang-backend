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