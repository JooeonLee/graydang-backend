package com.graydang.app.domain.user.controller;

import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.bill.model.dto.BillReactionResponseDto;
import com.graydang.app.domain.bill.model.dto.BillReactionSimpleResponseDto;
import com.graydang.app.domain.bill.model.dto.BillSimpleResponseDto;
import com.graydang.app.domain.bill.service.BillReactionService;
import com.graydang.app.domain.bill.service.BillScrapeService;
import com.graydang.app.domain.comment.model.dto.CommentSimpleResponseDto;
import com.graydang.app.domain.comment.service.CommentApplicationService;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.model.dto.NicknameCheckRequestDto;
import com.graydang.app.domain.user.model.dto.OnboardingRequestDto;
import com.graydang.app.domain.user.model.dto.UserInfoResponseDto;
import com.graydang.app.domain.user.service.UserProfileService;
import com.graydang.app.domain.user.service.UserService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.dto.SliceResponse;
import com.graydang.app.global.common.model.dto.ValidationErrorResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User-Controller", description = "User 관련 API 엔드포인트")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final BillScrapeService billScrapeService;
    private final BillReactionService billReactionService;
    private final CommentApplicationService commentApplicationService;

    @Operation(summary = "닉네임 중복 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 여부 반환"),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검사 실패",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
            )
    })
    @PostMapping("/nickname/check")
    public ResponseEntity<BaseResponse<Boolean>> checkNickname(
            @Valid @RequestBody NicknameCheckRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        boolean response = userProfileService.checkNickname(requestDto.nickname());
        return ResponseEntity.ok(BaseResponse.success(response));
    }


    @Operation(summary = "유저 온보딩", description = "로그인한 유저의 프로필 정보를 최초로 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "온보딩 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 실패",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
    })
    @PostMapping("/me/onboarding")
    public ResponseEntity<BaseResponse<Void>> onboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingRequestDto requestDto) {

        userProfileService.onboarding(userDetails.getId(), requestDto);
        return ResponseEntity.ok(BaseResponse.success(BaseResponseStatus.SUCCESS));
    }

    @Operation(summary = "유저 프로필 수정", description = "로그인한 유저의 프로필 정보를 수정합니다.")
    @PatchMapping("/me/info")
    public ResponseEntity<BaseResponse<Void>> updateUserProfileInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingRequestDto requestDto) {

        userProfileService.updateProfileInfo(userDetails.getId(), requestDto);
        return ResponseEntity.ok(BaseResponse.success(BaseResponseStatus.SUCCESS));
    }

    @Operation(summary = "마이페이지 유저 기본 정보")
    @GetMapping("/me/info")
    public ResponseEntity<BaseResponse<UserInfoResponseDto>> getUserProfileInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponseDto responseDto = userProfileService.getUserProfileInfo(userDetails.getId());
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "마이페이지 유저 북마크 정보")
    @GetMapping("/me/bookmarks")
    public ResponseEntity<BaseResponse<SliceResponse<BillSimpleResponseDto>>> getBillScrapeInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "16") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        SliceResponse<BillSimpleResponseDto> responseDto = billScrapeService.getScrapedBillsByUserId(userDetails.getId(), pageRequest);
        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "마이페이지 유저 의견 정보")
    @GetMapping("/me/reactions")
    public ResponseEntity<BaseResponse<SliceResponse<BillReactionSimpleResponseDto>>> getBillReactionInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "16") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        SliceResponse<BillReactionSimpleResponseDto> responseDto = billReactionService.getBillReactionInfoByUserId(userDetails.getId(), pageRequest);

        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "마이페이지 유저 댓글 정보")
    @GetMapping("/me/comments")
    public ResponseEntity<BaseResponse<SliceResponse<CommentSimpleResponseDto>>> getBillCommentInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "16") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        SliceResponse<CommentSimpleResponseDto> responseDto = commentApplicationService.getCommentInfoByUserId(userDetails, pageRequest);

        return ResponseEntity.ok(BaseResponse.success(responseDto));
    }

    @Operation(summary = "마이페이지 유저 프로필 이미지 수정")
    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> updateUserProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(description = "새 프로필 이미지 파일 (null 이면 기본 이미지로 변경)", required = false, example = "image.jpeg")
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        String imageUrl = userProfileService.updateProfileImage(userDetails, profileImage);

        return ResponseEntity.ok(BaseResponse.success(imageUrl));
    }
}
