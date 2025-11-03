package com.graydang.app.domain.user.repository;

import com.graydang.app.common.fixture.UserTestDataBuilder;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserCredential;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.model.enums.UserStatus;
import com.graydang.app.domain.user.model.enums.WithdrawalReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("사용자 저장 및 조회 테스트")
    void testSaveAndFindUser() {
        // Given
        User user = UserTestDataBuilder.createActiveUser();
        
        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("사용자명으로 조회 테스트")
    void testFindByUsername() {
        // Given
        User user = UserTestDataBuilder.createActiveUser();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("OAuth Provider로 사용자 조회 테스트")
    void testFindByProviderAndProviderUserId() {
        // Given
        User user = UserTestDataBuilder.createUserWithCredentials();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // When
        Optional<User> foundUser = userRepository.findByProviderAndProviderUserId("google", "google123");
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getCredentials()).hasSize(2);
    }
    
    @Test
    @DisplayName("존재하지 않는 Provider로 조회 시 빈 Optional 반환")
    void testFindByProviderAndProviderUserId_NotFound() {
        // Given
        User user = UserTestDataBuilder.createUserWithCredentials();
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // When
        Optional<User> foundUser = userRepository.findByProviderAndProviderUserId("facebook", "facebook123");
        
        // Then
        assertThat(foundUser).isEmpty();
    }
    
    @Test
    @DisplayName("User 저장 시 UserProfile도 함께 저장되는지 테스트")
    void testCascadeSaveUserProfile() {
        // Given
        User user = UserTestDataBuilder.createUserWithProfile();
        
        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(foundUser.getProfile()).isNotNull();
        assertThat(foundUser.getProfile().getNickname()).isEqualTo("테스트유저");
        assertThat(foundUser.getProfile().getKeyword1()).isEqualTo("정치");
        assertThat(foundUser.getProfile().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("User 저장 시 UserCredential 리스트도 함께 저장되는지 테스트")
    void testCascadeSaveUserCredentials() {
        // Given
        User user = UserTestDataBuilder.createUserWithCredentials();
        
        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(foundUser.getCredentials()).hasSize(2);
        assertThat(foundUser.getCredentials())
                .extracting(UserCredential::getProvider)
                .containsExactlyInAnyOrder("google", "kakao");
    }
    
    @Test
    @DisplayName("User 상태 변경이 정상적으로 저장되는지 테스트")
    void testUserStatusUpdate() {
        // Given
        User user = UserTestDataBuilder.createActiveUser();
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // When
        User userToUpdate = userRepository.findById(savedUser.getId()).orElseThrow();
        userToUpdate.withdraw(WithdrawalReason.NO_LONGER_NEEDED, null);
        userRepository.save(userToUpdate);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(updatedUser.getWithdrawalReason()).isEqualTo(WithdrawalReason.NO_LONGER_NEEDED);
        assertThat(updatedUser.getWithdrawalOtherReason()).isNull();
    }
    
    @Test
    @DisplayName("User 탈퇴 시 기타 사유 저장 테스트")
    void testUserWithdrawWithOtherReason() {
        // Given
        User user = UserTestDataBuilder.createActiveUser();
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // When
        User userToUpdate = userRepository.findById(savedUser.getId()).orElseThrow();
        String otherReason = "서비스가 마음에 들지 않습니다";
        userToUpdate.withdraw(WithdrawalReason.OTHER, otherReason);
        userRepository.save(userToUpdate);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(updatedUser.getWithdrawalReason()).isEqualTo(WithdrawalReason.OTHER);
        assertThat(updatedUser.getWithdrawalOtherReason()).isEqualTo(otherReason);
    }
    
    @Test
    @DisplayName("연관된 엔티티들의 상태도 함께 변경되는지 테스트")
    void testCascadeStatusUpdate() {
        // Given
        User user = UserTestDataBuilder.createFullUser();
        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        
        // When
        User userToUpdate = userRepository.findById(savedUser.getId()).orElseThrow();
        userToUpdate.block();
        userToUpdate.getProfile().block();
        userToUpdate.getCredentials().forEach(UserCredential::block);
        userRepository.save(userToUpdate);
        entityManager.flush();
        entityManager.clear();
        
        // Then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(updatedUser.getProfile().getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(updatedUser.getCredentials())
                .extracting(UserCredential::getStatus)
                .containsOnly(UserStatus.BLOCKED);
    }
    
    @Test
    @DisplayName("사용자명 존재 여부 확인 테스트")
    void testExistsByUsername() {
        // Given
        User user = UserTestDataBuilder.createActiveUser();
        userRepository.save(user);
        entityManager.flush();
        
        // When & Then
        assertThat(userRepository.existsByUsername("testuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }
    
    @Test
    @DisplayName("사용자명 또는 이메일로 검색 테스트")
    void testFindByUsernameContainingOrEmailContaining() {
        // Given
        User user1 = UserTestDataBuilder.createActiveUserWithId(1L);
        User user2 = UserTestDataBuilder.createActiveUserWithId(2L);
        User user3 = UserTestDataBuilder.createActiveUserWithId(3L);
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        entityManager.flush();
        
        // When
        Page<User> result = userRepository.findByUsernameContainingOrEmailContaining(
                "testuser", "test", PageRequest.of(0, 10));
        
        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}