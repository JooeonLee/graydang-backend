package com.graydang.app.domain.user.service;

import com.graydang.app.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public boolean checkNickname(String nickname) {
        return userProfileRepository.existsByNickname(nickname);
    }
}
