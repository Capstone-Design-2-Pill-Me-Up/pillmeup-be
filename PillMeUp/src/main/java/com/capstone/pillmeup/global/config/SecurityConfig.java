package com.capstone.pillmeup.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.capstone.pillmeup.global.exception.security.ExceptionFilter;
import com.capstone.pillmeup.global.exception.security.jwt.JwtAccessDeniedHandler;
import com.capstone.pillmeup.global.exception.security.jwt.JwtAuthenticationHandler;
import com.capstone.pillmeup.global.exception.security.jwt.JwtFilter;
import com.capstone.pillmeup.global.exception.security.oauth.CustomOAuth2UserService;
import com.capstone.pillmeup.global.exception.security.oauth.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ExceptionFilter exceptionFilter;
    private final JwtFilter jwtFilter;
    private final JwtAuthenticationHandler jwtAuthenticationHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler; 
	
	@Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    	http
    		// CORS 설정
    		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
    	
	        // 세션 미사용 (JWT)
	        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        
	        // CSRF/폼로그인/H2 콘솔 프레임
	        .csrf(csrf -> csrf.disable())
	        .formLogin(fl -> fl.disable())
	        .headers(h -> h.frameOptions(f -> f.disable()))
	        
	        // 인증/인가 실패 응답 핸들러
	        .exceptionHandling(eh -> eh
	            .authenticationEntryPoint(jwtAuthenticationHandler) // 401
	            .accessDeniedHandler(jwtAccessDeniedHandler)        // 403
	        )
	        
	        // 경로 인가 규칙 (JwtFilter의 whitelist와 반드시 일치)
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers(
	        		"/api/health",
	                "/api/auth/**",
	                "/h2-console/**",
	                "/swagger-ui/**",
	                "/v3/api-docs/**",
	                "/oauth2/**",                 // OAuth2 authorize UI
	                "/oauth2/authorization/**",   // 명시적 허용
	                "/login/oauth2/**"            // OAuth2 콜백
	            ).permitAll()
	            .anyRequest().authenticated()
	        )
	        
	        // 소셜 로그인(OAuth2) — 카카오/네이버: 성공 시 AT 발급 후 프론트로 리다이렉트
	        .oauth2Login(oauth -> oauth
	            .userInfoEndpoint(ui -> ui.userService(customOAuth2UserService)) // 사용자 로딩/업서트
	            .successHandler(oAuth2SuccessHandler)                            // AT 발급 및 리다이렉트
	            .failureHandler((req, res, ex) -> res.sendError(401, ex.getMessage()))
	        )
	        
	        // 예외 필터 → JWT 필터 → UsernamePasswordAuthenticationFilter
	        .addFilterBefore(exceptionFilter, UsernamePasswordAuthenticationFilter.class)
	        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 
		return http.build();
		
	}
    
    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    	
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 개발/운영 도메인 허용
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:61855",  // 로컬 개발용
                "http://localhost:3000",  // 로컬 개발용
                "https://wonsandbox.cloud" // 배포 서버
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // JWT 쿠키 전달 가능
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
        
    }
	
}
