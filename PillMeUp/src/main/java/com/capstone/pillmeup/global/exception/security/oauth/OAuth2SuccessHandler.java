package com.capstone.pillmeup.global.exception.security.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.capstone.pillmeup.global.exception.response.ApiResponse;
import com.capstone.pillmeup.global.exception.security.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.success-redirect}")
    private String successRedirectBase; // http://localhost:3000/social/callback
    
    // true면 항상 JSON으로 응답 (운영에서는 false 권장)
    @Value("${app.oauth2.debug-json:false}")
    private boolean debugJson;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {

        OAuth2User principal = (OAuth2User) auth.getPrincipal();
        Object memberIdObj = principal.getAttribute("memberId");
        Object providerObj = principal.getAttribute("provider");
        if (memberIdObj == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Long memberId = (memberIdObj instanceof Number)
                ? ((Number) memberIdObj).longValue()
                : Long.valueOf(memberIdObj.toString());
        String provider = providerObj == null ? null : providerObj.toString();

        String access = jwtProvider.generateAccessToken(memberId); // AT만 발급

        // 디버그 JSON 모드이거나, 리다이렉트 경로가 비어있으면 JSON으로 바로 응답
        if (debugJson || successRedirectBase == null || successRedirectBase.isBlank()) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(
                res.getOutputStream(),
                ApiResponse.success(Map.of(
                    "accessToken", access,
                    "memberId", memberId,
                    "provider", provider
                ))
            );
            return;
        }

        // 프론트로 리다이렉트
        String redirect = successRedirectBase + "?token=" + URLEncoder.encode(access, StandardCharsets.UTF_8);
        res.sendRedirect(redirect);
        
    }
}
