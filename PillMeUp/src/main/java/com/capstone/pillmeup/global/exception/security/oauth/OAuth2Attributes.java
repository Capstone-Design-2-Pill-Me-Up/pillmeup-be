package com.capstone.pillmeup.global.exception.security.oauth;

import java.util.Map;

import com.capstone.pillmeup.domain.user.entity.Provider;

import lombok.Builder;

@Builder
public record OAuth2Attributes(
        Provider provider,
        String providerId,
        String name,    // 닉네임
        Map<String, Object> attributes,
        String nameAttributeKey
) {
    public static OAuth2Attributes of(String registrationId, Map<String, Object> attrs) {
        Provider p = Provider.valueOf(registrationId.toUpperCase());
        return switch (p) {
            case KAKAO -> fromKakao(attrs);
            case NAVER -> fromNaver(attrs);
            default -> throw new IllegalArgumentException("지원하지 않는 Provider: " + registrationId);
        };
    }

    @SuppressWarnings("unchecked")
    private static OAuth2Attributes fromKakao(Map<String, Object> a) {
        String providerId = String.valueOf(a.get("id"));
        Map<String, Object> account = (Map<String, Object>) a.get("kakao_account");
        Map<String, Object> profile = account != null ? (Map<String, Object>) account.get("profile") : null;

        String name  = profile != null ? (String) profile.get("nickname") : null;    // 닉네임

        return OAuth2Attributes.builder()
                .provider(Provider.KAKAO)
                .providerId(providerId)
                .name(name)
                .attributes(a)
                .nameAttributeKey("id")
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuth2Attributes fromNaver(Map<String, Object> a) {
        Map<String, Object> resp = (Map<String, Object>) a.get("response");
        String providerId = resp != null ? (String) resp.get("id") : null;
        String name       = resp != null ? (String) resp.get("name") : null;  // 이름/닉네임

        return OAuth2Attributes.builder()
                .provider(Provider.NAVER)
                .providerId(providerId)
                .name(name)
                .attributes(a)
                .nameAttributeKey("response")
                .build();
    }
    
}
