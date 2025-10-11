package com.capstone.pillmeup.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
}
