package com.graydang.app.domain.admin.service;

import com.graydang.app.domain.admin.dto.UserDetailResponseDto;
import com.graydang.app.domain.admin.dto.UserListResponseDto;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.global.common.exception.BusinessException;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserAdminService {

    private final UserRepository userRepository;

    public Page<UserListResponseDto> getUserList(Pageable pageable, String keyword) {
        Page<User> users;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingOrEmailContaining(
                    keyword.trim(), keyword.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(UserListResponseDto::from);
    }

    public UserDetailResponseDto getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(BaseResponseStatus.NONE_USER));
        
        return UserDetailResponseDto.from(user);
    }
}