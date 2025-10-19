package com.capstone.pillmeup.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.capstone.pillmeup.domain.user.dto.request.SignInRequest;
import com.capstone.pillmeup.domain.user.dto.request.SignUpRequest;
import com.capstone.pillmeup.domain.user.dto.response.MemberResponse;
import com.capstone.pillmeup.domain.user.entity.Member;
import com.capstone.pillmeup.domain.user.entity.Provider;
import com.capstone.pillmeup.domain.user.repository.MemberRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;
import com.capstone.pillmeup.global.exception.security.jwt.JwtProvider;
import com.capstone.pillmeup.global.exception.security.jwt.dto.TokenResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    
    @Value("${spring.security.oauth2.client.registration.naver.client-id:}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret:}")
    private String naverClientSecret;
	
    // 회원가입 (LOCAL)
    @Transactional
    public MemberResponse signup(SignUpRequest req) {
        // 수동 검증 (@Valid 미사용)
        if (req == null || isBlank(req.getEmail()) || isBlank(req.getPassword()) || isBlank(req.getName())) {
            throw new CoreException(ErrorType.VALIDATION_ERROR);
        }

        // 중복 체크: email + LOCAL
        if (memberRepository.existsByEmailAndProvider(req.getEmail(), Provider.LOCAL)) {
            throw new CoreException(ErrorType.MEMBER_ALREADY_EXISTS);
        }

        // 엔티티 생성 (LOCAL: providerId = email)
        Member member = Member.local(
                req.getEmail(),
                passwordEncoder.encode(req.getPassword()),
                req.getName()
        );
        // (선택) 명시적으로 타입 세팅
        member = memberRepository.save(member);

        return MemberResponse.from(member);
    }
    
    // 로그인 (LOCAL)
    @Transactional(readOnly = true)
    public TokenResponse signin(SignInRequest req) {
        // 수동 검증
        if (req == null || isBlank(req.getEmail()) || isBlank(req.getPassword())) {
            throw new CoreException(ErrorType.VALIDATION_ERROR);
        }

        Member member = memberRepository.findByEmailAndProvider(req.getEmail(), Provider.LOCAL)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            // 탈퇴 계정은 동일 응답으로 숨김
            throw new CoreException(ErrorType.MEMBER_NOT_FOUND);
        }
        if (!member.isActive()) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }
        if (!passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            throw new CoreException(ErrorType.PASSWORD_MISMATCH);
        }

        String accessToken = jwtProvider.generateAccessToken(member.getMemberId());
        return new TokenResponse(accessToken);
    }
    
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    
    @Transactional(readOnly = true)
    public void logout(String token, String provider) {
        if (provider == null || provider.equalsIgnoreCase("LOCAL")) {
            handleLocalLogout(token);
        } else if (provider.equalsIgnoreCase("KAKAO")) {
            handleKakaoLogout(token);
        } else if (provider.equalsIgnoreCase("NAVER")) {
            handleNaverLogout(token);
        } else {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "지원하지 않는 provider 값입니다.");
        }
    }

    // Local 로그아웃: JWT 검증 후 성공 처리
    private void handleLocalLogout(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CoreException(ErrorType.INVALID_TOKEN);
        }

        String rawToken = token.substring(7);
        if (!jwtProvider.validateToken(rawToken)) {
            throw new CoreException(ErrorType.INVALID_TOKEN);
        }

        // 서버 세션이 없기 때문에 여기서는 단순히 성공 반환
        // (Redis를 사용하면 블랙리스트 저장 가능)
    }

    // Kakao 로그아웃
    private void handleKakaoLogout(String accessToken) {
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new CoreException(ErrorType.INVALID_TOKEN);
        }

        String rawToken = accessToken.substring(7);
        try {
            WebClient.create("https://kapi.kakao.com/v1/user/logout")
                    .post()
                    .header("Authorization", "Bearer " + rawToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new CoreException(ErrorType.EXTERNAL_API_ERROR_KAKAO);
        }
    }

    // Naver 로그아웃
    private void handleNaverLogout(String accessToken) {
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new CoreException(ErrorType.INVALID_TOKEN);
        }

        String rawToken = accessToken.substring(7);
        try {
            WebClient.create("https://nid.naver.com/oauth2.0/token")
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("grant_type", "delete")
                            .queryParam("client_id", naverClientId)
                            .queryParam("client_secret", naverClientSecret)
                            .queryParam("access_token", rawToken)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new CoreException(ErrorType.EXTERNAL_API_ERROR_NAVER);
        }
    }

    
}
