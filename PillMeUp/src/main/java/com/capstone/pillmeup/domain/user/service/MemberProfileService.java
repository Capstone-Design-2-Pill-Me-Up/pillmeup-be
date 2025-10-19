package com.capstone.pillmeup.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pillmeup.domain.user.dto.request.MemberPasswordRequest;
import com.capstone.pillmeup.domain.user.dto.request.MemberProfileRequest;
import com.capstone.pillmeup.domain.user.dto.response.MemberProfileResponse;
import com.capstone.pillmeup.domain.user.entity.Member;
import com.capstone.pillmeup.domain.user.entity.Provider;
import com.capstone.pillmeup.domain.user.repository.MemberRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

	private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
	
    // 프로필 조회
    public MemberProfileResponse getProfile(Provider provider, String providerId) {
        Member member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        return MemberProfileResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .provider(member.getProvider())
                .build();
    }

    // 이름 수정 (이메일은 수정 불가)
    @Transactional
    public void updateProfile(Provider provider, String providerId, MemberProfileRequest req) {
        Member member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new CoreException(ErrorType.INVALID_INPUT, "이름은 비워둘 수 없습니다.");
        }

        // 엔티티를 변경하기 위해 Member에 별도 도메인 메서드 추가 권장
        member.changeName(req.getName());
    }

    // 비밀번호 변경 (LOCAL 전용)
    @Transactional
    public void changePassword(Provider provider, String providerId, MemberPasswordRequest req) {
        Member member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        // 소셜 로그인 사용자는 비밀번호가 없음
        if (member.getProvider() != Provider.LOCAL) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        if (!passwordEncoder.matches(req.getCurrentPassword(), member.getPassword())) {
            throw new CoreException(ErrorType.PASSWORD_MISMATCH);
        }

        member.changePassword(passwordEncoder.encode(req.getNewPassword()));
    }

    // 회원 탈퇴 (isDeleted = true / isActive = false)
    @Transactional
    public void deleteAccount(Provider provider, String providerId) {
        Member member = memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        member.markDeleted();
        member.deactivate();
    }
    
}
