# 회원 관리 기능 테스트 계획서

작성일: 2025-10-01  
작성자: Claude

## 개요

이 문서는 Graypick 애플리케이션의 회원 탈퇴 및 차단 기능에 대한 Repository 테스트와 통합 테스트 계획을 포함합니다.

## 테스트 대상 기능

### 1. 회원 탈퇴 (User Withdrawal)
- **API Endpoint**: `DELETE /api/users/me`
- **Service**: `UserService.withdrawUser()`
- **주요 로직**:
  - User, UserProfile, UserCredential의 status를 INACTIVE로 변경
  - 이미 탈퇴한 사용자 재탈퇴 방지
  - 차단된 사용자도 탈퇴 가능

### 2. 회원 차단 (User Blocking)
- **API Endpoints**: 
  - `PUT /api/admin/users/{userId}/block` (차단)
  - `PUT /api/admin/users/{userId}/unblock` (차단 해제)
- **Service**: `AdminService.blockUser()`, `AdminService.unblockUser()`
- **주요 로직**:
  - User, UserProfile, UserCredential의 status를 BLOCKED로 변경
  - 탈퇴한 사용자는 차단 불가
  - ADMIN 권한 필요

## Repository 테스트 계획

### UserRepository 테스트

#### 1. 기본 CRUD 테스트
```java
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    
    @Test
    @DisplayName("사용자 저장 및 조회 테스트")
    void testSaveAndFindUser() {
        // Given: User 엔티티 생성
        // When: save() 호출
        // Then: findById()로 조회 후 검증
    }
    
    @Test
    @DisplayName("사용자명으로 조회 테스트")
    void testFindByUsername() {
        // Given: 특정 username을 가진 User 저장
        // When: findByUsername() 호출
        // Then: Optional<User> 검증
    }
    
    @Test
    @DisplayName("OAuth Provider로 사용자 조회 테스트")
    void testFindByProviderAndProviderUserId() {
        // Given: UserCredential과 함께 User 저장
        // When: findByProviderAndProviderUserId() 호출
        // Then: 결과 검증
    }
}
```

#### 2. 연관 엔티티 Cascade 테스트
```java
@Test
@DisplayName("User 저장 시 UserProfile도 함께 저장되는지 테스트")
void testCascadeSaveUserProfile() {
    // Given: UserProfile이 설정된 User
    // When: userRepository.save(user)
    // Then: UserProfile도 자동으로 저장됨
}

@Test
@DisplayName("User 저장 시 UserCredential 리스트도 함께 저장되는지 테스트")
void testCascadeSaveUserCredentials() {
    // Given: UserCredential 리스트가 설정된 User
    // When: userRepository.save(user)
    // Then: 모든 UserCredential도 자동으로 저장됨
}
```

#### 3. 상태 변경 테스트
```java
@Test
@DisplayName("User 상태 변경이 정상적으로 저장되는지 테스트")
void testUserStatusUpdate() {
    // Given: ACTIVE 상태의 User
    // When: user.withdraw() 후 save()
    // Then: 데이터베이스에서 INACTIVE 상태로 조회됨
}

@Test
@DisplayName("연관된 엔티티들의 상태도 함께 변경되는지 테스트")
void testCascadeStatusUpdate() {
    // Given: UserProfile, UserCredential과 연관된 User
    // When: 각 엔티티의 상태 변경 후 save()
    // Then: 모든 엔티티의 상태가 데이터베이스에 반영됨
}
```

## Service 테스트 계획

### UserService 테스트

#### 1. 정상 탈퇴 시나리오
```java
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {
    
    @Test
    @DisplayName("정상적인 회원 탈퇴 처리")
    void testWithdrawUser_Success() {
        // Given: ACTIVE 상태의 User
        // When: withdrawUser() 호출
        // Then: 
        //   - User status = INACTIVE
        //   - UserProfile status = INACTIVE
        //   - 모든 UserCredential status = INACTIVE
    }
    
    @Test
    @DisplayName("차단된 사용자도 탈퇴 가능")
    void testWithdrawUser_BlockedUser() {
        // Given: BLOCKED 상태의 User
        // When: withdrawUser() 호출
        // Then: 정상적으로 INACTIVE로 변경
    }
}
```

#### 2. 예외 시나리오
```java
@Test
@DisplayName("존재하지 않는 사용자 탈퇴 시도")
void testWithdrawUser_NonExistentUser() {
    // Given: 존재하지 않는 userId
    // When: withdrawUser() 호출
    // Then: UserException(NONE_USER) 발생
}

@Test
@DisplayName("이미 탈퇴한 사용자 재탈퇴 시도")
void testWithdrawUser_AlreadyWithdrawn() {
    // Given: INACTIVE 상태의 User
    // When: withdrawUser() 호출
    // Then: UserException(ALREADY_WITHDRAWN_USER) 발생
}
```

### AdminService 테스트

#### 1. 사용자 차단 테스트
```java
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AdminServiceTest {
    
    @Test
    @DisplayName("정상적인 사용자 차단")
    void testBlockUser_Success() {
        // Given: ACTIVE 상태의 User
        // When: blockUser() 호출
        // Then:
        //   - User status = BLOCKED
        //   - UserProfile status = BLOCKED
        //   - 모든 UserCredential status = BLOCKED
    }
    
    @Test
    @DisplayName("이미 차단된 사용자 재차단 시도")
    void testBlockUser_AlreadyBlocked() {
        // Given: BLOCKED 상태의 User
        // When: blockUser() 호출
        // Then: UserException(ALREADY_BLOCKED_USER) 발생
    }
    
    @Test
    @DisplayName("탈퇴한 사용자 차단 시도")
    void testBlockUser_InactiveUser() {
        // Given: INACTIVE 상태의 User
        // When: blockUser() 호출
        // Then: UserException(CANNOT_BLOCK_INACTIVE_USER) 발생
    }
}
```

#### 2. 사용자 차단 해제 테스트
```java
@Test
@DisplayName("정상적인 차단 해제")
void testUnblockUser_Success() {
    // Given: BLOCKED 상태의 User
    // When: unblockUser() 호출
    // Then: 모든 엔티티의 status = ACTIVE
}

@Test
@DisplayName("차단되지 않은 사용자 차단 해제 시도")
void testUnblockUser_NotBlocked() {
    // Given: ACTIVE 상태의 User
    // When: unblockUser() 호출
    // Then: UserException(NOT_BLOCKED_USER) 발생
}
```

## 통합 테스트 계획

### 1. 회원 탈퇴 API 테스트

#### 정상 시나리오
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {
    
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("회원 탈퇴 API 성공 테스트")
    void testWithdrawUser_Success() {
        // Given: 인증된 사용자
        // When: DELETE /api/users/me
        // Then: 
        //   - 200 OK 응답
        //   - 데이터베이스에서 사용자 상태 확인
    }
    
    @Test
    @DisplayName("인증되지 않은 사용자의 탈퇴 시도")
    void testWithdrawUser_Unauthorized() {
        // Given: 인증 없음
        // When: DELETE /api/users/me
        // Then: 401 Unauthorized
    }
}
```

#### 트랜잭션 롤백 테스트
```java
@Test
@WithMockUser
@DisplayName("탈퇴 처리 중 예외 발생 시 롤백")
void testWithdrawUser_TransactionRollback() {
    // Given: UserCredential 업데이트 시 예외 발생하도록 설정
    // When: DELETE /api/users/me
    // Then: 
    //   - 500 에러 응답
    //   - User, UserProfile 상태가 변경되지 않음 (롤백 확인)
}
```

### 2. 관리자 차단 API 테스트

#### 권한 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("관리자 권한으로 사용자 차단 성공")
    void testBlockUser_AdminSuccess() {
        // Given: ADMIN 권한
        // When: PUT /api/admin/users/{userId}/block
        // Then: 200 OK
    }
    
    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("일반 사용자가 차단 시도 시 권한 없음")
    void testBlockUser_AccessDenied() {
        // Given: USER 권한
        // When: PUT /api/admin/users/{userId}/block
        // Then: 403 Forbidden
    }
}
```

#### 비즈니스 로직 통합 테스트
```java
@Test
@WithMockUser(roles = "ADMIN")
@DisplayName("차단 → 탈퇴 시나리오 통합 테스트")
void testBlockThenWithdraw_Integration() {
    // Given: 정상 사용자
    // When: 
    //   1. 관리자가 차단 (PUT /api/admin/users/{userId}/block)
    //   2. 사용자가 탈퇴 시도 (DELETE /api/users/me)
    // Then: 
    //   - 차단된 상태에서도 탈퇴 가능
    //   - 최종 상태 INACTIVE
}

@Test
@WithMockUser(roles = "ADMIN")
@DisplayName("탈퇴 → 차단 시나리오 통합 테스트")
void testWithdrawThenBlock_Integration() {
    // Given: 정상 사용자
    // When:
    //   1. 사용자가 탈퇴 (DELETE /api/users/me)
    //   2. 관리자가 차단 시도 (PUT /api/admin/users/{userId}/block)
    // Then:
    //   - 탈퇴한 사용자는 차단 불가
    //   - 400 Bad Request (CANNOT_BLOCK_INACTIVE_USER)
}
```

### 3. 동시성 테스트

```java
@Test
@DisplayName("동시에 여러 요청이 들어올 때의 처리")
void testConcurrentRequests() {
    // Given: 여러 스레드 준비
    // When: 동시에 차단/차단해제/탈퇴 요청
    // Then: 
    //   - 데이터 일관성 유지
    //   - 적절한 예외 처리
}
```

## 테스트 데이터 준비

### TestDataBuilder 패턴 사용
```java
public class UserTestDataBuilder {
    public static User createActiveUser() {
        return User.builder()
            .username("testuser")
            .role("ROLE_USER")
            .status(UserStatus.ACTIVE)
            .build();
    }
    
    public static User createUserWithProfile() {
        User user = createActiveUser();
        UserProfile profile = UserProfile.builder()
            .user(user)
            .nickname("테스트유저")
            .keyword1("정치")
            .status(UserStatus.ACTIVE)
            .build();
        user.setProfile(profile);
        return user;
    }
    
    public static User createUserWithCredentials() {
        User user = createActiveUser();
        List<UserCredential> credentials = List.of(
            UserCredential.builder()
                .user(user)
                .provider("google")
                .providerUserId("google123")
                .status(UserStatus.ACTIVE)
                .build(),
            UserCredential.builder()
                .user(user)
                .provider("kakao")
                .providerUserId("kakao123")
                .status(UserStatus.ACTIVE)
                .build()
        );
        user.setCredentials(credentials);
        return user;
    }
}
```

## 테스트 실행 계획

### 1. 단계별 실행
1. Repository 테스트 먼저 작성 및 실행
2. Service 단위 테스트 작성 및 실행
3. Controller 통합 테스트 작성 및 실행
4. 전체 테스트 스위트 실행

### 2. 테스트 커버리지 목표
- Repository 레이어: 90% 이상
- Service 레이어: 85% 이상
- Controller 레이어: 80% 이상
- 전체: 85% 이상

### 3. CI/CD 파이프라인 통합
```yaml
# GitHub Actions 예시
- name: Run tests
  run: ./gradlew test
  
- name: Generate test report
  run: ./gradlew jacocoTestReport
  
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
```

## 주의사항

1. **트랜잭션 관리**: 모든 테스트는 `@Transactional`을 사용하여 롤백 처리
2. **테스트 격리**: 각 테스트는 독립적으로 실행되어야 함
3. **테스트 데이터**: H2 in-memory DB 사용, 테스트 후 자동 정리
4. **Mock 사용**: 외부 의존성(S3, Redis 등)은 Mock 처리
5. **보안 테스트**: Spring Security Test 활용하여 권한 검증

## 향후 개선사항

1. **성능 테스트**: 대량 사용자 차단/탈퇴 시 성능 측정
2. **부하 테스트**: JMeter 등을 활용한 API 부하 테스트
3. **E2E 테스트**: Selenium 등을 활용한 UI 포함 전체 플로우 테스트
4. **모니터링**: 테스트 실행 시간 및 안정성 모니터링