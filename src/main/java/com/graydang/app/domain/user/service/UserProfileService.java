package com.graydang.app.domain.user.service;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
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
import com.graydang.app.global.s3.model.ImagePrefix;
import com.graydang.app.global.s3.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final ImageService imageService;

    @Value("${user.default-profile-image-url}")
    private String DEFAULT_USER_IMAGE_URL;

    public boolean checkNickname(String nickname) {
        return userProfileRepository.existsByNickname(nickname);
    }

    @Transactional
    public void onboarding(Long userId, OnboardingRequestDto requestDto) {
        if(userProfileRepository.existsByUserId(userId)) {
            throw new UserProfileException(BaseResponseStatus.ALREADY_ONBOARDED);
        }

        List<InterestKeyword> keywords = requestDto.interestKeywords().stream()
                .map(InterestKeyword::fromLabel)
                .toList();

        User user = userService.findByIdOrThrow(userId);

        String[] keywordNames = new String[5];
        for(int i=0; i<keywords.size(); i++) {
            keywordNames[i] = keywords.get(i).name();
        }

        UserProfile userProfile = UserProfile.builder()
                .nickname(requestDto.nickname())
                .keyword1(keywordNames[0])
                .keyword2(keywordNames[1])
                .keyword3(keywordNames[2])
                .keyword4(keywordNames[3])
                .keyword5(keywordNames[4])
                .profileImage(DEFAULT_USER_IMAGE_URL)
                .user(user)
                .status("ACTIVE")
                .build();

        userProfileRepository.save(userProfile);
    }

    @Transactional
    public void updateProfileInfo(Long userId, OnboardingRequestDto requestDto) {
        List<InterestKeyword> keywords = requestDto.interestKeywords().stream()
                .map(InterestKeyword::fromLabel)
                .toList();

        User user = userService.findByIdOrThrow(userId);

        String[] keywordNames = new String[5];
        for(int i=0; i<keywords.size(); i++) {
            keywordNames[i] = keywords.get(i).name();
        }

        UserProfile currentUserProfile = getUserProfileByUserId(userId);
        currentUserProfile.updateProfileInfo(
                requestDto.nickname(),
                keywordNames[0],
                keywordNames[1],
                keywordNames[2],
                keywordNames[3],
                keywordNames[4]
        );
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

    @Transactional
    public String updateProfileImage(CustomUserDetails userDetails, MultipartFile image) {
        UserProfile userProfile = getUserProfileByUserId(userDetails.getId());

        String currentImage = userProfile.getProfileImage();
        if(!DEFAULT_USER_IMAGE_URL.equals(currentImage)) {
            imageService.delete(currentImage);
        }

        if(image == null || image.isEmpty()) {
            userProfile.updateProfileImage(DEFAULT_USER_IMAGE_URL);
            return DEFAULT_USER_IMAGE_URL;
        }

        String newImageUrl = imageService.upload(image, ImagePrefix.USER_PROFILE);
        userProfile.updateProfileImage(newImageUrl);

        return newImageUrl;
    }

    public Set<String> getUserKeywordsByUserId(Long userId) {
        UserProfile userProfile = getUserProfileByUserId(userId);

        return Stream.of(
                userProfile.getKeyword1(),
                userProfile.getKeyword2(),
                userProfile.getKeyword3(),
                userProfile.getKeyword4(),
                userProfile.getKeyword5())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
