package com.capstone.pillmeup.domain.user.dto.response;

import java.time.LocalDateTime;

import com.capstone.pillmeup.domain.user.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {

	private Long memberId;
    private String provider;     // enum -> 문자열
    private String providerId;
    private String email;
    private String name;
    private String memberType;   // enum -> 문자열
    private boolean active;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberResponse from(Member m) {
        return MemberResponse.builder()
                .memberId(m.getMemberId())
                .provider(m.getProvider() != null ? m.getProvider().name() : null)
                .providerId(m.getProviderId())
                .email(m.getEmail())
                .name(m.getName())
                .memberType(m.getMemberType() != null ? m.getMemberType().name() : null)
                .active(m.isActive())
                .deleted(m.isDeleted())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
	
}
