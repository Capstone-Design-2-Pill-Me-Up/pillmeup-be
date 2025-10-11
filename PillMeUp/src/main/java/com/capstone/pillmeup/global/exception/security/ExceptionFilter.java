package com.capstone.pillmeup.global.exception.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;
import com.capstone.pillmeup.global.exception.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExceptionFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			filterChain.doFilter(request, response);

        } catch (CoreException e) { // 우리 프로젝트 예외
            writeError(response, e.getErrorType());

        } catch (ExpiredJwtException e) { // 만료
            writeError(response, ErrorType.UNAUTHORIZED);

        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            // 서명 불일치/형식 오류/지원X/파라미터 오류 → 토큰 문제로 통일
            writeError(response, ErrorType.INVALID_TOKEN);

        } catch (Exception e) { // 그밖의 예외
            writeError(response, ErrorType.INTERNAL_SERVER_ERROR);
        }
		
	}
	
	private void writeError(HttpServletResponse response, ErrorType type) throws IOException {
        response.setStatus(type.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(type));
    }
	
}
