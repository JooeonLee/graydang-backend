package com.graydang.app.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graydang.app.domain.auth.exception.InvalidTokenException;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.auth.util.JwtUtil;
import com.graydang.app.domain.user.exception.UserException;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AntPathMatcher antPathMatcher =  new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public final static List<String> PASS_URIS = Arrays.asList(
            "/api/auth/oauth/**",
            "/api/auth/reissue",
            "/api/auth/logout",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.info("[JWT Filter] Incoming request: {}", requestUri);

        if (isPassUri(requestUri)) {
            log.info("[JWT Filter] Skipping auth check for pass URI: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveToken(request);

        if (!StringUtils.hasText(accessToken)) {
            log.info("[JWT Filter] No token provided. Passing request.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 만료 검사
            if (jwtUtil.isExpired(accessToken)) {
                throw new InvalidTokenException(BaseResponseStatus.EXPIRED_TOKEN);
            }

            // 타입 검사
            if (!JwtUtil.TOKEN_TYPE_ACCESS.equals(jwtUtil.getTokenType(accessToken))) {
                log.warn("[JWT Filter] Invalid token type.");
                throw new InvalidTokenException(BaseResponseStatus.INVALID_TOKEN_TYPE);
            }

            // 사용자 정보 추출
            Long userId = jwtUtil.getUserId(accessToken);
            String provider = jwtUtil.getProvider(accessToken);
            String providerId = jwtUtil.getProviderId(accessToken);

            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("[JWT Filter] User not found for userId: {}", userId);
                        return new UserException(BaseResponseStatus.NONE_USER);
                    });

            // 인증 객체 설정
            CustomUserDetails userDetails = new CustomUserDetails(user, provider, providerId, null);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("[JWT Filter] Authentication successful for userId: {}", userId);

        } catch (InvalidTokenException | UserException e) {
            log.warn("[JWT Filter] Authentication failed: {}", e.getBaseResponseStatus().getResponseMessage());
            handleErrorResponse(response, e.getBaseResponseStatus());
            return;
        } catch (Exception e) {
            log.warn("[JWT Filter] Invalid JWT: {}", e.getMessage());
            handleErrorResponse(response, BaseResponseStatus.INVALID_TOKEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private boolean isPassUri(String uri) {

        return PASS_URIS.stream().anyMatch(pattern -> antPathMatcher.match(pattern, uri));
    }

    private void handleErrorResponse(HttpServletResponse response, BaseResponseStatus status) throws IOException {
        response.setStatus(status.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        BaseResponse<Object> baseResponse = new BaseResponse<>(status);
        response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
    }
}
