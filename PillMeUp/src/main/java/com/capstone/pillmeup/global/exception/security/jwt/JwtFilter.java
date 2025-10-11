package com.capstone.pillmeup.global.exception.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;
import com.capstone.pillmeup.global.exception.security.AuthDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;               // AT 전용 (sub = memberId)
    private final AuthDetailsService authDetailsService; // memberId 기반 UserDetails 로드

    // 토큰 없이 접근 허용할 경로 (필요에 맞게 수정)
    private static final List<String> WHITELIST = List.of(
    		"/api/auth/**",
            "/h2-console/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/oauth2/**",                 // OAuth2 authorize UI
            "/oauth2/authorization/**",   // 명시적 허용
            "/login/oauth2/**"            // OAuth2 콜백
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
	
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    	String uri = request.getRequestURI();
    	return WHITELIST.stream().anyMatch(p -> PATH_MATCHER.match(p, uri));
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = resolveBearer(request); // "Authorization: Bearer <AT>"

        // 헤더가 없으면 통과 (보호 API는 SecurityConfig에서 401로 걸림)
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 헤더는 있는데 토큰이 유효하지 않음 → 표준 에러로 처리
        if (!jwtProvider.validateToken(token)) {
            throw new CoreException(ErrorType.INVALID_TOKEN);
        }

        // 유효: sub(memberId) → 유저 조회 → SecurityContext 세팅
        Long memberId = jwtProvider.getMemberId(token);
        UserDetails user = authDetailsService.loadUserByUsername(String.valueOf(memberId));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
        
    }
    
	private String resolveBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
	
}
