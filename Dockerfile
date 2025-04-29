# 🔧 1단계: Build stage
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# 빌드에 필요한 모든 파일 복사
COPY . .

# 테스트 제외하고 빌드 (필요 시 테스트 포함 가능)
RUN ./gradlew clean build -x test


# 🚀 2단계: Run stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드된 jar 복사 (최종 이미지에 gradle 등은 포함되지 않음)
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Boot 기본 포트
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

