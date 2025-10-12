package com.capstone.pillmeup.domain.user.dto.response;

import java.time.LocalDateTime;

import com.capstone.pillmeup.domain.user.entity.Member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "회원 정보 응답")
public class MemberResponse {

	@Schema(description = "회원 PK", example = "123")
    private Long memberId;

    @Schema(description = "가입 제공자(LOCAL/KAKAO/NAVER 등)", example = "LOCAL")
    private String provider;

    @Schema(description = "제공자 식별자(LOCAL의 경우 email)", example = "user1@example.com")
    private String providerId;

    @Schema(description = "이메일", example = "user1@example.com")
    private String email;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "회원 유형(USER/ADMIN 등)", example = "USER")
    private String memberType;

    @Schema(description = "활성 여부", example = "true")
    private boolean active;

    @Schema(description = "삭제(탈퇴) 여부", example = "false")
    private boolean deleted;

    @Schema(description = "생성 시각", example = "2025-10-12T13:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2025-10-12T13:00:00")
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
