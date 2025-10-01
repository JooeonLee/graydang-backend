package com.graydang.app.domain.user.service;

import com.graydang.app.common.fixture.UserTestDataBuilder;
import com.graydang.app.domain.user.exception.UserException;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserCredential;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.model.enums.UserStatus;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void testFindByIdOrThrow_Success() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createActiveUserWithId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        User foundUser = userService.findByIdOrThrow(userId);
        
        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(userId);
        verify(userRepository, times(1)).findById(userId);
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
    void testFindByIdOrThrow_NotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.findByIdOrThrow(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.NONE_USER);
        
        verify(userRepository, times(1)).findById(userId);
    }
    
    @Test
    @DisplayName("정상적인 회원 탈퇴 처리")
    void testWithdrawUser_Success() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createFullUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        userService.withdrawUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getProfile().getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getCredentials())
                .extracting(UserCredential::getStatus)
                .containsOnly(UserStatus.INACTIVE);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    @DisplayName("UserProfile이 없는 사용자도 정상 탈퇴 처리")
    void testWithdrawUser_WithoutProfile() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createUserWithCredentials();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        userService.withdrawUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getProfile()).isNull();
        assertThat(user.getCredentials())
                .extracting(UserCredential::getStatus)
                .containsOnly(UserStatus.INACTIVE);
        
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    @DisplayName("차단된 사용자도 탈퇴 가능")
    void testWithdrawUser_BlockedUser() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createFullUser();
        user.block();
        user.getProfile().block();
        user.getCredentials().forEach(UserCredential::block);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        userService.withdrawUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getProfile().getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getCredentials())
                .extracting(UserCredential::getStatus)
                .containsOnly(UserStatus.INACTIVE);
        
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 탈퇴 시도")
    void testWithdrawUser_NonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> userService.withdrawUser(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.NONE_USER);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("이미 탈퇴한 사용자 재탈퇴 시도")
    void testWithdrawUser_AlreadyWithdrawn() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createInactiveUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When & Then
        assertThatThrownBy(() -> userService.withdrawUser(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.ALREADY_WITHDRAWN_USER);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("UserCredential이 없는 사용자도 정상 탈퇴 처리")
    void testWithdrawUser_WithoutCredentials() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createUserWithProfile();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        userService.withdrawUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getProfile().getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getCredentials()).isEmpty();
        
        verify(userRepository, times(1)).save(user);
    }
}