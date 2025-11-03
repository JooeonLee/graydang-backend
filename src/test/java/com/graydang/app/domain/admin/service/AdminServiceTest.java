package com.graydang.app.domain.admin.service;

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
class AdminServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AdminService adminService;
    
    @Test
    @DisplayName("정상적인 사용자 차단")
    void testBlockUser_Success() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createFullUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        adminService.blockUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.getProfile().getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.getCredentials())
                .extracting(UserCredential::getStatus)
                .containsOnly(UserStatus.BLOCKED);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    @DisplayName("UserProfile이 없는 사용자도 정상 차단 처리")
    void testBlockUser_WithoutProfile() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createUserWithCredentials();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        adminService.blockUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.getProfile()).isNull();
        assertThat(user.getCredentials())
                .extracting(UserCredential::getStatus)
                .containsOnly(UserStatus.BLOCKED);
        
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    @DisplayName("이미 차단된 사용자 재차단 시도")
    void testBlockUser_AlreadyBlocked() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createBlockedUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When & Then
        assertThatThrownBy(() -> adminService.blockUser(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.ALREADY_BLOCKED_USER);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("탈퇴한 사용자 차단 시도")
    void testBlockUser_InactiveUser() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createInactiveUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When & Then
        assertThatThrownBy(() -> adminService.blockUser(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.CANNOT_BLOCK_INACTIVE_USER);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 차단 시도")
    void testBlockUser_NonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> adminService.blockUser(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.NONE_USER);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("정상적인 차단 해제")
    void testUnblockUser_Success() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createFullUser();
        user.block();
        user.getProfile().block();
        user.getCredentials().forEach(UserCredential::block);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        adminService.unblockUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getProfile().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCredentials())
                .extracting(UserCredential::getStatus)
                .containsOnly(UserStatus.ACTIVE);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    @DisplayName("차단되지 않은 사용자 차단 해제 시도")
    void testUnblockUser_NotBlocked() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createActiveUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When & Then
        assertThatThrownBy(() -> adminService.unblockUser(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.NOT_BLOCKED_USER);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 차단 해제 시도")
    void testUnblockUser_NonExistentUser() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> adminService.unblockUser(userId))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("baseResponseStatus", BaseResponseStatus.NONE_USER);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("UserCredential이 없는 사용자의 차단 및 차단 해제")
    void testBlockAndUnblockUser_WithoutCredentials() {
        // Given
        Long userId = 1L;
        User user = UserTestDataBuilder.createUserWithProfile();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When - 차단
        adminService.blockUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.getProfile().getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.getCredentials()).isEmpty();
        
        // When - 차단 해제
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        adminService.unblockUser(userId);
        
        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getProfile().getStatus()).isEqualTo(UserStatus.ACTIVE);
        
        verify(userRepository, times(2)).save(user);
    }
}