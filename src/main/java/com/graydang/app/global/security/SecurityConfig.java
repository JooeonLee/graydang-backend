package com.graydang.app.global.security;

import com.graydang.app.domain.auth.filter.JwtFilter;
import com.graydang.app.domain.auth.util.JwtUtil;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    //private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) // For H2 console
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/", "/error", "/favicon.ico", "/h2-console/**", "/health/**").permitAll()
                    .requestMatchers("/api/auth/oauth/**", "/api/auth/refresh").permitAll()
                    .requestMatchers("/api/public/**", "/api/test/public").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/bills/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            )
            //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 