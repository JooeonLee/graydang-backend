package com.graydang.app.domain.user.service;

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
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public User findByIdOrThrow(Long id) {
        return  userRepository.findById(id).orElseThrow(() -> new UserException(BaseResponseStatus.NONE_USER));
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = findByIdOrThrow(userId);
        
        // 이미 탈퇴한 사용자인지 확인
        if (user.getStatus().isInactive()) {
            throw new UserException(BaseResponseStatus.ALREADY_WITHDRAWN_USER);
        }
        
        // User 상태를 INACTIVE로 변경
        user.withdraw();
        
        // UserProfile 상태를 INACTIVE로 변경
        UserProfile profile = user.getProfile();
        if (profile != null) {
            profile.deactivate();
        }
        
        // 모든 UserCredential 상태를 INACTIVE로 변경
        for (UserCredential credential : user.getCredentials()) {
            credential.deactivate();
        }
        
        userRepository.save(user);
        log.info("User withdrawn successfully: userId={}", userId);
    }
}
