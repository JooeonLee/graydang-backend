package com.graydang.app.domain.user.service;

import com.graydang.app.domain.user.model.InterestKeyword;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.model.dto.OnboardingRequestDto;
import com.graydang.app.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserService userService;

    final String DEFAULT_USER_IMAGE_URL = "DEFAULT_USER_IMAGE";

    public boolean checkNickname(String nickname) {
        return userProfileRepository.existsByNickname(nickname);
    }

    @Transactional
    public void onboarding(Long userId, OnboardingRequestDto requestDto) {
        List<InterestKeyword> keywords = requestDto.interestKeywords().stream()
                .map(InterestKeyword::fromLabel)
                .toList();

        User user = userService.findByIdOrThrow(userId);

        UserProfile userProfile = UserProfile.builder()
                .nickname(requestDto.nickname())
                .keyword1(keywords.get(0).name())
                .keyword2(keywords.get(1).name())
                .keyword3(keywords.get(2).name())
                .keyword4(keywords.get(3).name())
                .keyword5(keywords.get(4).name())
                .profileImage(DEFAULT_USER_IMAGE_URL)
                .user(user)
                .status("ACTIVE")
                .build();

        userProfileRepository.save(userProfile);
    }
}
