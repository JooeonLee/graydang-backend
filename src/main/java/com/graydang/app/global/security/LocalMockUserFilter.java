//package com.graydang.app.global.security;
//
//import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
//import com.graydang.app.domain.user.model.User;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Map;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//public class LocalMockUserFilter extends OncePerRequestFilter {
//
//  @Override
//  protected void doFilterInternal(
//      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
//  ) throws ServletException, IOException {
//
//    // 이미 인증이 있으면 건드리지 않음
//    if (SecurityContextHolder.getContext().getAuthentication() == null) {
//      // 최소 필드만 세팅된 가짜 User (빌더/생성자 방식은 도메인에 맞게 수정)
//      User mock = User.builder()
//          .id(1L)
//          .username("local-dev")
//          .role("ADMIN")
//          .build();
//
//      CustomUserDetails cud = new CustomUserDetails(mock, "local", "local", Map.of());
//      UsernamePasswordAuthenticationToken auth =
//          new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());
//      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//      SecurityContextHolder.getContext().setAuthentication(auth);
//    }
//
//    filterChain.doFilter(request, response);
//  }
//}
