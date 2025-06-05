package com.graydang.app.domain.user.controller;

import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.model.dto.NicknameCheckRequestDto;
import com.graydang.app.domain.user.service.UserProfileService;
import com.graydang.app.domain.user.service.UserService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User-Controller", description = "User 관련 API 엔드포인트")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @PostMapping("/nickname/check")
    public ResponseEntity<BaseResponse<Boolean>> checkNickname(
            @Valid @RequestBody NicknameCheckRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        boolean response = userProfileService.checkNickname(requestDto.nickname());
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
