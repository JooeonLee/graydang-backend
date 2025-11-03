package com.graydang.app.domain.admin.service;

import com.graydang.app.domain.user.exception.UserException;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserCredential;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public void blockUser(Long userId) {
        User user = findUserByIdOrThrow(userId);
        
        // 이미 차단된 사용자인지 확인
        if (user.getStatus().isBlocked()) {
            throw new UserException(BaseResponseStatus.ALREADY_BLOCKED_USER);
        }
        
        // 탈퇴한 사용자는 차단할 수 없음
        if (user.getStatus().isInactive()) {
            throw new UserException(BaseResponseStatus.CANNOT_BLOCK_INACTIVE_USER);
        }
        
        // User 차단 처리
        user.block();
        
        // UserProfile 차단 처리
        UserProfile profile = user.getProfile();
        if (profile != null) {
            profile.block();
        }
        
        // 모든 UserCredential 차단 처리
        for (UserCredential credential : user.getCredentials()) {
            credential.block();
        }
        
        userRepository.save(user);
        log.info("User blocked successfully: userId={}", userId);
    }
    
    @Transactional
    public void unblockUser(Long userId) {
        User user = findUserByIdOrThrow(userId);
        
        // 차단되지 않은 사용자인지 확인
        if (!user.getStatus().isBlocked()) {
            throw new UserException(BaseResponseStatus.NOT_BLOCKED_USER);
        }
        
        // User 차단 해제 처리
        user.unblock();
        
        // UserProfile 차단 해제 처리
        UserProfile profile = user.getProfile();
        if (profile != null) {
            profile.unblock();
        }
        
        // 모든 UserCredential 차단 해제 처리
        for (UserCredential credential : user.getCredentials()) {
            credential.unblock();
        }
        
        userRepository.save(user);
        log.info("User unblocked successfully: userId={}", userId);
    }
    
    private User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(BaseResponseStatus.NONE_USER));
    }
}