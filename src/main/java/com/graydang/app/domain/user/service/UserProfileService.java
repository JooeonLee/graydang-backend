package com.graydang.app.domain.user.service;

import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.service.BillReactionService;
import com.graydang.app.domain.bill.service.BillScrapeService;
import com.graydang.app.domain.comment.service.CommentService;
import com.graydang.app.domain.user.exception.UserProfileException;
import com.graydang.app.domain.user.model.InterestKeyword;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.model.dto.OnboardingRequestDto;
import com.graydang.app.domain.user.model.dto.UserInfoResponseDto;
import com.graydang.app.domain.user.repository.UserProfileRepository;
import com.graydang.app.global.common.model.dto.SliceResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.graydang.app.global.common.model.enums.BaseResponseStatus.NONE_USER_PROFILE;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserService userService;
    private final BillScrapeService billScrapeService;
    private final BillReactionService billReactionService;
    private final CommentService commentService;

    @Value("${user.default-profile-image-url}")
    private String DEFAULT_USER_IMAGE_URL;

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

    public String getNicknameByUserId(Long userId) {
        return userProfileRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .map(UserProfile::getNickname)
                .orElse(null);
    }

    public UserInfoResponseDto getUserProfileInfo(Long userId) {
        userService.findByIdOrThrow(userId);
        UserProfile userProfile = getUserProfileByUserId(userId);
        long scrapeCount = billScrapeService.getBillScrapeCountByUserId(userId);
        long reactionCount = billReactionService.getBillReactionCountByUserId(userId);
        long commentCount = commentService.getCommentCountByUserId(userId);

        return UserInfoResponseDto.of(userProfile, scrapeCount, reactionCount, commentCount);
    }

//    public SliceResponse<BillSimpleResponseDto> getBillScrapeByUserId(Long userId) {
//
//
//    }

    public UserProfile getUserProfileByUserId(Long userId) {
        return userProfileRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new UserProfileException(NONE_USER_PROFILE));
    }
}
