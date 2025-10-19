package com.capstone.pillmeup.global.exception.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.capstone.pillmeup.domain.user.entity.Provider;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;               // AT 전용 (sub = memberId)

    // 토큰 없이 접근 허용할 경로 (필요에 맞게 수정)
    private static final List<String> WHITELIST = List.of(
    		"/api/health",
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
            FilterChain chain)
		throws ServletException, IOException {
		
		String token = resolveBearer(request);
		
		if (!StringUtils.hasText(token)) {
		chain.doFilter(request, response);
		return;
		}
		
		if (!jwtProvider.validateToken(token)) {
		throw new CoreException(ErrorType.INVALID_TOKEN);
		}
		
		// JWT Claims 추출
		Claims claims = jwtProvider.parseClaims(token);
		
		Long memberId = Long.valueOf(claims.getSubject());
		Provider provider = Provider.valueOf(claims.get("provider", String.class));
		String providerId = claims.get("providerId", String.class);
		
		CustomUserPrincipal principal = new CustomUserPrincipal(memberId, provider, providerId);
		
		// Authentication 객체 생성 (권한은 USER 고정)
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(principal, null, List.of(() -> "USER"));
		
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
