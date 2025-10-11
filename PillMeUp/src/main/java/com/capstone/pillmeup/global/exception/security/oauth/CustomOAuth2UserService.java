package com.capstone.pillmeup.global.exception.security.oauth;

import java.util.Map;
import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.capstone.pillmeup.domain.user.entity.Member;
import com.capstone.pillmeup.domain.user.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberRepository memberRepository;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {

		OAuth2User o = super.loadUser(req);
        String registrationId = req.getClientRegistration().getRegistrationId(); // kakao/naver

        OAuth2Attributes attr = OAuth2Attributes.of(registrationId, o.getAttributes());

        // email fallback 조회 제거 — 고유 식별은 (provider, providerId)만 사용
        Member member = memberRepository
                .findByProviderAndProviderId(attr.provider(), attr.providerId())
                .orElseGet(() -> memberRepository.save(
                        Member.social(attr.provider(), attr.providerId(), attr.name())
                ));

        // 권한은 "USER"로 통일
        var authorities = Set.of(new SimpleGrantedAuthority("USER"));

        // 성공 핸들러가 토큰 발급할 때 꺼낼 값만 넣어 둠
        Map<String, Object> principalAttrs = Map.of(
                "memberId", member.getMemberId(),
                "provider", registrationId
        );

        // nameAttributeKey는 "memberId" 사용
        return new DefaultOAuth2User(authorities, principalAttrs, "memberId");
		
	}
	
}
