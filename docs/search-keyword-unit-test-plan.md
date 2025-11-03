# Search Keyword 단위 테스트 계획서

생성일: 2025-10-11  
목적: 검색 키워드 기능의 단위 테스트 및 통합 테스트 계획 수립

## 1. 테스트 대상 개요

### 1.1 주요 컴포넌트
- **Entity**: SearchKeyword
- **Service**: SearchKeywordService  
- **Repository**: SearchKeywordRepository
- **Controller**: SearchKeywordController (컨트롤러 통합테스트 제외)
- **DTOs**: SearchKeywordCreateRequestDto, SearchKeywordUpdateRequestDto, SearchKeywordResponseDto

### 1.2 핵심 기능
- 검색 키워드 CRUD 작업
- 우선순위 기반 정렬
- 소프트 삭제/복구
- 표시/비표시 관리
- 중복 검사

## 2. Entity 단위 테스트

### 2.1 SearchKeyword Entity Tests

**테스트 클래스**: `SearchKeywordTest`

#### 2.1.1 빌더 테스트
```java
@Nested
@DisplayName("SearchKeyword 빌더 테스트")
public class SearchKeywordBuilderTest {
    - builder_shouldCreateWithDefaultValues()
    - builder_shouldCreateWithCustomValues()
}
```

#### 2.1.2 비즈니스 메서드 테스트
```java
@Nested  
@DisplayName("표시 상태 변경 테스트")
public class DisplayStateTest {
    - show_shouldChangeDisplayYnToY()
    - hide_shouldChangeDisplayYnToN()
}

@Nested
@DisplayName("소프트 삭제 테스트")
public class SoftDeleteTest {
    - softDelete_shouldChangeDelYnToY()
    - restore_shouldChangeDelYnToN()
}

@Nested
@DisplayName("속성 변경 테스트")
public class PropertyChangeTest {
    - changePriority_shouldUpdatePriority()
    - changeText_shouldUpdateText()
}
```

## 3. Repository 단위 테스트

### 3.1 SearchKeywordRepository Tests

**테스트 클래스**: `SearchKeywordRepositoryTest`  
**상속**: `@DataJpaTest`

#### 3.1.1 조회 메서드 테스트
```java
@Nested
@DisplayName("findByIdAndDelYn 테스트")
public class FindByIdAndDelYnTest {
    - findByIdAndDelYn_shouldReturnKeywordWhenExists()
    - findByIdAndDelYn_shouldReturnEmptyWhenDeleted()
    - findByIdAndDelYn_shouldReturnEmptyWhenNotFound()
}

@Nested  
@DisplayName("findAllByDisplayYnAndDelYn 정렬 테스트")
public class FindAllByDisplayYnAndDelYnTest {
    - findAll_shouldReturnInPriorityOrder()
    - findAll_shouldReturnOnlyDisplayedKeywords()
    - findAll_shouldExcludeDeletedKeywords()
}
```

#### 3.1.2 검색 메서드 테스트
```java
@Nested
@DisplayName("searchKeywords 테스트")
public class SearchKeywordsTest {
    - searchKeywords_shouldFilterByDelYn()
    - searchKeywords_shouldFilterByDisplayYn()
    - searchKeywords_shouldFilterByKeywordContains()
    - searchKeywords_shouldReturnAllWhenNoFilters()
    - searchKeywords_shouldReturnPagedResults()
}
```

#### 3.1.3 중복 검사 테스트
```java
@Nested
@DisplayName("existsByTextAndDelYn 테스트")
public class ExistsByTextAndDelYnTest {
    - existsByTextAndDelYn_shouldReturnTrueWhenExists()
    - existsByTextAndDelYn_shouldReturnFalseWhenDeleted()
    - existsByTextAndDelYn_shouldReturnFalseWhenNotFound()
}
```

#### 3.1.4 카운트 테스트
```java
@Nested
@DisplayName("countByDisplayYnAndDelYn 테스트")
public class CountByDisplayYnAndDelYnTest {
    - count_shouldReturnCorrectCountForDisplayedKeywords()
    - count_shouldExcludeDeletedKeywords()
}
```

## 4. Service 단위 테스트

### 4.1 SearchKeywordService Tests

**테스트 클래스**: `SearchKeywordServiceTest`  
**의존성 모킹**: `@Mock SearchKeywordRepository`

#### 4.1.1 생성 메서드 테스트
```java
@Nested
@DisplayName("createSearchKeyword 테스트")
public class CreateSearchKeywordTest {
    - create_shouldThrowExceptionWhenKeywordAlreadyExists()
    - create_shouldSetPriorityBasedOnActiveCount()
    - create_shouldSaveWithCorrectDisplayStatus()
    - create_shouldReturnResponseDto()
}
```

#### 4.1.2 조회 메서드 테스트
```java
@Nested
@DisplayName("getSearchKeyword 테스트")
public class GetSearchKeywordTest {
    - get_shouldReturnKeywordWhenExists()
    - get_shouldThrowExceptionWhenNotFound()
    - get_shouldThrowExceptionWhenDeleted()
}

@Nested
@DisplayName("getDisplaySearchKeywords 테스트")
public class GetDisplaySearchKeywordsTest {
    - getDisplay_shouldReturnOnlyDisplayedKeywords()
    - getDisplay_shouldReturnInPriorityOrder()
    - getDisplay_shouldReturnEmptyListWhenNone()
}
```

#### 4.1.3 검색 메서드 테스트
```java
@Nested
@DisplayName("searchKeywords 테스트")
public class SearchKeywordsServiceTest {
    - search_shouldDelegateToRepository()
    - search_shouldReturnPagedResults()
    - search_shouldMapToResponseDto()
}
```

#### 4.1.4 수정 메서드 테스트
```java
@Nested
@DisplayName("updateSearchKeyword 테스트")
public class UpdateSearchKeywordTest {
    - update_shouldThrowExceptionWhenNotFound()
    - update_shouldUpdateTextWhenProvided()
    - update_shouldThrowExceptionWhenTextDuplicated()
    - update_shouldUpdatePriorityWhenProvided()
    - update_shouldUpdateDisplayStatusWhenProvided()
    - update_shouldUpdateDeleteStatusWhenProvided()
    - update_shouldOnlyUpdateProvidedFields()
}
```

## 5. 통합 테스트

### 5.1 Service 통합 테스트

**테스트 클래스**: `SearchKeywordServiceIntegrationTest`  
**설정**: `@SpringBootTest`, `@ActiveProfiles("test")`

#### 5.1.1 트랜잭션 테스트
```java
@Nested
@DisplayName("트랜잭션 통합 테스트")
public class TransactionIntegrationTest {
    - create_shouldRollbackOnException()
    - update_shouldPersistChanges()
    - softDelete_shouldNotActuallyDeleteRecord()
}
```

#### 5.1.2 우선순위 관리 테스트
```java
@Nested
@DisplayName("우선순위 시나리오 테스트")
public class PriorityScenarioTest {
    - multipleCreate_shouldAssignIncrementalPriorities()
    - priorityChange_shouldNotAffectSorting()
    - deleteAndCreate_shouldRecalculatePriority()
}
```

## 6. 테스트 데이터 설정

### 6.1 Fixture 클래스
```java
public class SearchKeywordFixture {
    public static SearchKeyword createDefault() {
        return SearchKeyword.builder()
            .text("테스트 키워드")
            .priority(1)
            .displayYn(Yn.Y)
            .delYn(Yn.N)
            .build();
    }
    
    public static SearchKeyword createWithText(String text) {
        return SearchKeyword.builder()
            .text(text)
            .priority(1)
            .build();
    }
    
    public static SearchKeyword createDeleted() {
        return SearchKeyword.builder()
            .text("삭제된 키워드")
            .delYn(Yn.Y)
            .build();
    }
}
```

## 7. 테스트 우선순위

### 7.1 필수 테스트 (High Priority)
1. SearchKeywordService의 CRUD 기본 작업
2. SearchKeywordRepository의 중복 검사
3. 소프트 삭제/복구 기능
4. 우선순위 기반 정렬

### 7.2 중요 테스트 (Medium Priority)
1. 검색 필터링 기능
2. 페이징 처리
3. 트랜잭션 롤백

### 7.3 추가 테스트 (Low Priority)
1. DTO 변환 로직
2. 빌더 패턴 검증
3. 엣지 케이스 처리

## 8. 테스트 커버리지 목표

- **Entity**: 100% (모든 비즈니스 메서드)
- **Repository**: 90% (커스텀 쿼리 중심)
- **Service**: 95% (핵심 비즈니스 로직)
- **전체 목표**: 85% 이상

## 9. 실행 계획

### Phase 1 (1주차)
- Entity 단위 테스트 작성
- Repository 단위 테스트 작성

### Phase 2 (2주차)
- Service 단위 테스트 작성
- Service 통합 테스트 작성

### Phase 3 (3주차)
- 테스트 리팩토링
- 커버리지 분석 및 보완
- 문서화 업데이트

## 10. 주의사항

1. **테스트 격리**: 각 테스트는 독립적으로 실행 가능해야 함
2. **데이터 정리**: @DirtiesContext 사용 시 주의
3. **트랜잭션**: 통합 테스트에서 @Transactional 사용 시 실제 동작과 차이 주의
4. **H2 호환성**: 실제 MySQL과 다른 동작 확인 필요
5. **시간 의존성**: createdAt, updatedAt은 DB 기본값 사용으로 테스트 시 주의

## 11. 예상 이슈 및 대응

### 11.1 날짜/시간 처리
- DB 자동 생성 시간 필드는 모킹하지 않고 존재 여부만 확인
- 필요 시 @CreatedDate, @LastModifiedDate 어노테이션 고려

### 11.2 페이징 테스트
- 실제 데이터 개수와 페이지 설정 확인
- Sort 조건 검증 추가

### 11.3 트랜잭션 경계
- Service 레이어의 @Transactional 동작 검증
- LazyLoading 이슈 확인