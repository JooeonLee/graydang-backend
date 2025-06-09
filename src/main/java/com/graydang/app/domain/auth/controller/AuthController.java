package com.graydang.app.domain.auth.controller;

import com.graydang.app.domain.auth.dto.LoginRequestDto;
import com.graydang.app.domain.auth.dto.LoginResponseDto;
import com.graydang.app.domain.auth.dto.ReissueTokenRequestDto;
import com.graydang.app.domain.auth.dto.ReissueTokenResponseDto;
import com.graydang.app.domain.auth.exception.InvalidTokenException;
import com.graydang.app.domain.auth.service.JwtService;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.user.repository.UserProfileRepository;
import com.graydang.app.domain.user.service.UserProfileService;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import com.graydang.app.global.security.jwt.JwtTokenProvider;
import com.graydang.app.domain.auth.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth-Controller", description = "OAuth2.0 인증 및 JWT 관련 API 엔드포인트")
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final JwtService jwtService;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;

    @PostMapping("/oauth/{provider}")
    @Operation(summary = "소셜 로그인", description = "Authorization Code로 소셜 로그인 처리 및 JWT 토큰 발급")
    public BaseResponse<LoginResponseDto> oauthLogin(
            @Parameter(description = "소셜 로그인 제공자", example = "kakao") @PathVariable String provider,
            @Valid @RequestBody LoginRequestDto request
    ) {
        var authentication = oAuth2Service.processOAuth2Login(provider, request.getCode(), request.getRedirectUri());
        var userDetails = (CustomUserDetails) authentication.getPrincipal();

        var tokens = jwtService.issueToken(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole(),
                userDetails.getProvider(),
                userDetails.getProviderId()
        );

        var nickname = userProfileService.getNicknameByUserId(userDetails.getId());
        var onboarded = userProfileRepository.findByUserIdAndStatus(userDetails.getId(), "ACTIVE").isPresent();

        var responseDto = new LoginResponseDto(
                tokens.get("accessToken"),
                tokens.get("refreshToken"),
                userDetails.getId(),
                nickname,
                onboarded
        );

        log.info("OAuth2 login successful for user: {} via provider: {}", userDetails.getUsername(), provider);
        return new BaseResponse<>(responseDto);
    }

    @PostMapping("/reissue")
    @Operation(summary = "JWT 재발급", description = "Refresh 토큰을 사용해 새로운 Access/Refresh 토큰 재발급")
    public BaseResponse<ReissueTokenResponseDto> refreshToken(@RequestBody ReissueTokenRequestDto request) {

        var newTokens = jwtService.reissueTokens(request.refreshToken());
        ReissueTokenResponseDto responseDto = new ReissueTokenResponseDto(newTokens.get("accessToken"),
                newTokens.get("refreshToken") );
        return new BaseResponse<>(responseDto);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록")
    public BaseResponse<String> logout(@RequestHeader("Authorization") String accessToken) {
        if (!StringUtils.hasText(accessToken) || !accessToken.startsWith("Bearer ")) {
            throw new InvalidTokenException(BaseResponseStatus.INVALID_TOKEN);
        }

        String token = accessToken.substring(7);
        jwtService.addToBlackList(token);
        return BaseResponse.success(BaseResponseStatus.SUCCESS);
    }

} 